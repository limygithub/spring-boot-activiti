package com.fengye.example.controller;

import com.fengye.example.service.FirstService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.OutputStream;

@RestController
public class FirstController {

    @Autowired
    private FirstService firstService;

    //开启流程实例
    @RequestMapping(value = "start", method = RequestMethod.GET)
    public void startProcessInstance() {
        firstService.start();
    }

    //获取当前节点名称
    @RequestMapping(value = "getName/{pid}", method = RequestMethod.GET)
    public String getTasks(@PathVariable String pid) {
        return firstService.getName(pid);
    }

    //提交审批
    @RequestMapping(value = "insert/{pid}", method = RequestMethod.GET)
    public void insert(@PathVariable String pid) {
        firstService.insert(pid);
    }

    //完成任务
    @RequestMapping(value = "audit/{auditFlag}/{pid}", method = RequestMethod.GET)
    public void complete(@PathVariable Boolean auditFlag, @PathVariable String pid) {
        firstService.audit(auditFlag, pid);
    }

    //获取当前审批进行到了哪一步，图片
    @RequestMapping(value = "image/{pid}", method = RequestMethod.GET)
    public void image(HttpServletResponse response, @PathVariable String pid) {
        try {
            InputStream is = firstService.getDiagram(pid);
            if (is == null){
                return;
            }

            response.setContentType("image/png");

            BufferedImage image = ImageIO.read(is);
            OutputStream out = response.getOutputStream();
            ImageIO.write(image, "png", out);

            is.close();
            out.close();
        } catch (Exception e) {
            System.out.println("查看流程图失败");
            e.printStackTrace();
        }
    }

}