package org.example.ringetty;

import cn.hutool.setting.Setting;
import org.example.ringetty.server.RingHttpServer;

import java.io.IOException;

/**
 * 此类提供框架整体服务，集成http服务端、数据库访问等必要组件
 */
public class RingettyServer {

    public static void main(String[] args) throws IOException {
        new RingHttpServer(new Setting("http.properties"));
    }

}
