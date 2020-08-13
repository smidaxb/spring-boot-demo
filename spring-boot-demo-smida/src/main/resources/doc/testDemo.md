之前测controller都是启起来用postman发请求，最近写单元测试的时候了解了一下mockmvc，写篇博客总结一下单元测试相关的东西。

# 1.常规配置
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
//@RunWith是JUnit的注解, 用来告诉JUnit使用指定的类做单元测试，不同的Runner类有其各自的功能
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

# 2.mock
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

## 2.1 mockmvc
MockMvc是由spring-test包提供，实现了对Http请求的模拟，能够直接使用网络的形式，转换到Controller的调用，使得测试速度快、不依赖网络环境。同时提供了一套验证的工具，结果的验证十分方便。

一个简单的示例：
例如：
```java
@Test
public void demoTest3() throws Exception {
    //mockMvc.perform执行一个请求；
    //MockMvcRequestBuilders.get("/url/u1")构造一个请求
    MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/url/u1"))
        //ResultActions.andExpect添加执行完成后的断言
        .andExpect(MockMvcResultMatchers.view().name("user/view"))
        .andExpect(MockMvcResultMatchers.model().attributeExists("username"))
        .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
        //ResultActions.andDo添加一个结果处理器，表示要对结果做点什么事情
        //比如此处使用MockMvcResultHandlers.print()输出整个响应结果信息。
        .andDo(MockMvcResultHandlers.print())
        //ResultActions.andReturn表示执行完成后返回相应的结果。
        .andReturn();
    Assert.assertNotNull(result.getModelAndView().getModel().get("username"));
}
```
基本流程：
1. 准备测试环境
2. 构造mock请求
3. 通过MockMvc执行请求
4. 添加验证断言，添加结果处理器
5. 得到MvcResult进行自定义断言/进行下一步的异步请求
6. 卸载测试环境

下边从各个类分析：

### 2.1.1 MockMvc
`MockMvc`是服务器端SpringMVC测试的主入口点。
核心方法：`perform(RequestBuilder rb)`，执行一个RequestBuilder请求，会自动执行SpringMVC的流程并映射到相应的控制器执行处理，该方法的返回值是一个`ResultActions`。
构造器类 `MockMvcBuilder`可用于构造`MockMvc`对象，主要有 `StandaloneMockMvcBuilder` 和`DefaultMockMvcBuilder`两个实现。
一般直接通过 `MockMVCBuilders` 工厂类的静态方法直接构建(如方式一)。
也可通过在测试类上使用 `@AutoConfigureMockMvc` 注解，自动配置，搭配 `@Autowired` 注入(方式二)。
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
        //可以直接指定一组控制器，也可以从上下文中获取控制器
        //mockMvc = MockMvcBuilders.standaloneSetup(XXXController.class).build();
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();  //构造MockMvc
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

### 2.1.2 请求构造(MockMvcRequestBuilder)
顾名思义，用来构建`Request`请求。主要有两个子类 `MockHttpServletRequestBuilder` 和 `MockMultipartHttpServletRequestBuilder`（如文件上传使用），用来Mock Request 需要的所有数据。
实际使用中既可以使用工厂类`MockMvcRequestBuilders`直接构造，也可以分别使用构造器。

- MockMvcRequestBuilders 主要API
```java
//根据uri模板和uri变量值得到一个GET请求方式的MockHttpServletRequestBuilder；如get(/user/{id}, 1L)
MockHttpServletRequestBuilder get(String url, Object... urlVariables)

//同get类似
MockHttpServletRequestBuilder post(String url, Object... urlVariables)
MockHttpServletRequestBuilder put(String url, Object... urlVariables)
MockHttpServletRequestBuilder delete(String url, Object... urlVariables) 
MockHttpServletRequestBuilder options(String url, Object... urlVariables)

// 提供自己的Http请求方法及uri模板和uri变量，如上API都是委托给这个API
MockHttpServletRequestBuilder request(HttpMethod httpMethod, String url, Object... urlVariables)

//提供文件上传方式的请求，得到MockMultipartHttpServletRequestBuilder
MockMultipartHttpServletRequestBuilder multipart(String url, Object... urlVariables)

//创建一个从启动异步处理的请求的MvcResult进行异步分派的RequestBuilder
RequestBuilder asyncDispatch(final MvcResult mvcResult)
```

- MockHttpServletRequestBuilder API
```java
MockHttpServletRequestBuilder header(String name, Object... values)
MockHttpServletRequestBuilder headers(HttpHeaders httpHeaders) 

MockHttpServletRequestBuilder contentType(MediaType mediaType)

MockHttpServletRequestBuilder accept(MediaType... mediaTypes)
MockHttpServletRequestBuilder accept(String... mediaTypes)//指定请求的Accept头信息

MockHttpServletRequestBuilder content(byte[] content)
MockHttpServletRequestBuilder content(String content)//指定请求Body体内容

MockHttpServletRequestBuilder cookie(Cookie... cookies)//指定请求的Cookie

MockHttpServletRequestBuilder locale(Locale locale)//指定请求的Locale

MockHttpServletRequestBuilder characterEncoding(String encoding)//指定请求字符编码

MockHttpServletRequestBuilder requestAttr(String name, Object value) //设置请求属性数据

MockHttpServletRequestBuilder sessionAttr(String name, Object value)
MockHttpServletRequestBuilder sessionAttrs(Map<string, object=""> sessionAttributes)//设置请求session属性数据

MockHttpServletRequestBuilder flashAttr(String name, Object value)
MockHttpServletRequestBuilder flashAttrs(Map<string, object=""> flashAttributes)//指定请求的flash信息，比如重定向后的属性信息

MockHttpServletRequestBuilder session(MockHttpSession session) //指定请求的Session

MockHttpServletRequestBuilder principal(Principal principal) //指定请求的Principal

MockHttpServletRequestBuilder contextPath(String contextPath) //指定请求的上下文路径，必须以“/”开头，且不能以“/”结尾

MockHttpServletRequestBuilder pathInfo(String pathInfo) //请求的路径信息，必须以“/”开头

MockHttpServletRequestBuilder secure(boolean secure)//请求是否使用安全通道

MockHttpServletRequestBuilder with(RequestPostProcessor postProcessor)//请求的后处理器，用于自定义一些请求处理的扩展点
```

- MockMultipartHttpServletRequestBuilder API
```java
//继承自MockHttpServletRequestBuilder，又提供了如下API
MockMultipartHttpServletRequestBuilder file(MockMultipartFile file)
MockMultipartHttpServletRequestBuilder part(Part... parts)
```

### 2.1.3 结果处理
- ResultActions
调用`MockMvc.perform(RequestBuilder requestBuilder)` 后将得到 `ResultActions`,可完成三件事：
```java
//添加验证断言来判断执行请求后的结果是否是预期的
ResultActions andExpect(ResultMatcher matcher) 

//添加结果处理器，用于对验证成功后执行的动作，如输出下请求/结果信息用于调试
ResultActions andDo(ResultHandler handler)

//返回验证成功后的MvcResult；用于自定义验证/下一步的异步处理
MvcResult andReturn()
```

- ResultMatcher/MockMvcResultMatchers
`ResultMatcher` 用来匹配执行完请求后的结果验证。通过`match(MvcResult result)`断言方法，匹配失败将抛出相应的异常
`spring mvc`提供了很多`***ResultMatchers`来满足测试需求，但其并不是`ResultMatcher`的子类，而是返回`ResultMatcher`实例
一般为了方便操作，直接使用`MockMvcResultMatchers`类的静态工厂方法来测试:

```java
HandlerResultMatchers handler()//请求的Handler验证器，比如验证处理器类型/方法名；此处的Handler其实就是处理请求的控制器

RequestResultMatchers request()//得到RequestResultMatchers验证器

ModelResultMatchers model()//得到模型验证器

ViewResultMatchers view()//得到视图验证器

FlashAttributeResultMatchers flash()//得到Flash属性验证

StatusResultMatchers status()//得到响应状态验证器

HeaderResultMatchers header()//得到响应Header验证器

CookieResultMatchers cookie()//得到响应Cookie验证器

ContentResultMatchers content()//得到响应内容验证器

JsonPathResultMatchers jsonPath(String expression, Object ... args)/ResultMatcher jsonPath  (String expression, Matcher matcher)//得到Json表达式验证器

XpathResultMatchers xpath(String expression, Object... args)/XpathResultMatchers xpath(String expression, Map<string, string=""> namespaces, Object... args)//得到Xpath表达式验证器

ResultMatcher forwardedUrl(final String expectedUrl)//验证处理完请求后转发的url（绝对匹配）

ResultMatcher forwardedUrlPattern(final String urlPattern)//验证处理完请求后转发的url（Ant风格模式匹配，@since spring4）

ResultMatcher redirectedUrl(final String expectedUrl)//验证处理完请求后重定向的url（绝对匹配）

ResultMatcher redirectedUrlPattern(final String expectedUrl)//验证处理完请求后重定向的url（Ant风格模式匹配，@since spring4）
```
各种请求方式的mockMvc demo如下：
