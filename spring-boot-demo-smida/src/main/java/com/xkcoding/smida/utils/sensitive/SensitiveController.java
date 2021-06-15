package com.xkcoding.smida.utils.sensitive;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Created by YangYifan on 2021/6/15.
 */
@Controller
@RequestMapping("/sensitive")
@Slf4j
public class SensitiveController {
    @Resource
    SensitiveService sensitiveService;

    @RequestMapping("/has")
    @ResponseBody
    public boolean hasSensitive(String txt) {
        return sensitiveService.hasSensitiveWords(txt);
    }

    @RequestMapping("/findAll")
    @ResponseBody
    public List<String> findAllSensitiveWord(String txt) {
        List<String> res = sensitiveService.findAllSensitiveWords(txt);
        log.info("findAllSensitiveWord| txt:{}, res:{}", txt, res);
        return res;
    }
}
