# springboot单元测试
之前测controller都是启起来用postman发请求，最近写单元测试的时候了解了一下mockmvc，写篇博客总结一下单元测试相关的东西。

## 1.常规配置
首先要在pom文件中引入springboot的test相关starter
```
 <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
 </dependency>
```
测试类的注解如下：
```java
//@ActiveProfiles可以指定运行环境
//@RunWith是JUnit的注解, 用来告诉JUnit使用指定的类做单元测试
//SpringRunner.class等同于SpringJUnit4ClassRunner.class
//@SpringBootTest 用来指定SpringBoot应用程序的入口类
@ActiveProfiles("dev")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = DemoApplication.class)
public class DemoTest {
    @Test
    public void testMethod(){
    }   
}
```
2.2.*版本后，springboot默认使用Junit5，区别如下表：

| JUnit4 | JUnit5 | 说明 |
| --- | --- | --- |
| @Test | @Test | 表示该方法是一个测试方法。JUnit5与JUnit 4的@Test注解不同的是，它没有声明任何属性，因为JUnit Jupiter中的测试扩展是基于它们自己的专用注解来完成的。这样的方法会被继承，除非它们被覆盖 |
| @BeforeClass | @BeforeAll | 表示使用了该注解的方法应该在当前类中所有使用了@Test @RepeatedTest、@ParameterizedTest或者@TestFactory注解的方法之前 执行； |
| @AfterClass | @AfterAll | 表示使用了该注解的方法应该在当前类中所有使用了@Test、@RepeatedTest、@ParameterizedTest或者@TestFactory注解的方法之后执行； |
| @Before | @BeforeEach | 表示使用了该注解的方法应该在当前类中每一个使用了@Test、@RepeatedTest、@ParameterizedTest或者@TestFactory注解的方法之前 执行 |
| @After | @AfterEach | 表示使用了该注解的方法应该在当前类中每一个使用了@Test、@RepeatedTest、@ParameterizedTest或者@TestFactory注解的方法之后 执行 |
| @Ignore | @Disabled | 用于禁用一个测试类或测试方法 |
| @Category | @Tag | 用于声明过滤测试的tags，该注解可以用在方法或类上；类似于TesgNG的测试组或JUnit 4的分类。 |
| @Parameters | @ParameterizedTest | 表示该方法是一个参数化测试 |
| @RunWith | @ExtendWith | @Runwith就是放在测试类名之前，用来确定这个类怎么运行的 |
| @Rule | @ExtendWith | Rule是一组实现了TestRule接口的共享类，提供了验证、监视TestCase和外部资源管理等能力 |
| @ClassRule | @ExtendWith | @ClassRule用于测试类中的静态变量，必须是TestRule接口的实例，且访问修饰符必须为public。 |

## 2.mockmvc
mock是指在测试过程中，对于一些不容易构造/获取的对象，创建一个mock对象来模拟对象的行为

- 在做单元测试过程中，经常会有以下的场景：
```
class A dependence class B
class B dependence class C
class C dependence class D
class D dependence ...
```
若此时我们想对class A进行单元测试，需要构造大量的class B、C、D等依赖对象，他们的构造过程复杂(体现在构造步骤多、耗时较长)，这时我们可以利用mock去构造虚拟的class B、C、D对象用于class A的测试，因为我们只是想测试class A的行为是否符合预期，我们并不需要测试依赖对象。

- 此外，有时被测单元依赖的模块尚未开发完成，而被测对象需要依赖模块的返回值进行测试：
比如service层的代码中，包含对dao层的调用，但dao层代码尚未开发
比如web的前端依赖后端接口获取数据进行联调测试，但后端接口并未开发完成

- 哪些时机和场合需要使用mock
单元测试/接口测试中测试对象依赖其他对象，这些对象的构造复杂、耗时或者根本无法构造(未交付)
我们只测试对象内部逻辑的质量，不关心依赖对象的逻辑正确性和稳定性

MockMvc是由spring-test包提供，实现了对Http请求的模拟，能够直接使用网络的形式，转换到Controller的调用，使得测试速度快、不依赖网络环境。同时提供了一套验证的工具，结果的验证十分方便。

mockmvc的初始化方式有两种
```java
//方式一
@SpringBootTest
@ActiveProfiles
@RunWith(SpringRunner.class)
@WebAppConfiguration
public class DemoTest1 {
    @Autowired
    public WebApplicationContext context;
    public MockMvc mockMvc;
    public Cookie[] cookies;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).alwaysDo(print()).build();  //构造MockMvc
        cookies = new Cookie[3];
        Cookie cookie1 = new Cookie("SESSION", "dev-test");
        cookies[0] = cookie1;
    }
}
//方式二
@SpringBootTest
@ActiveProfiles
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc //该注解将会自动配置mockMvc的单元测试
public class DemoTest1 {
    @Autowired
    public MockMvc mockMvc;
}
```
