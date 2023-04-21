package com.example.projet_heimdall;
import com.fazecast.jSerialComm.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Locale;

public class test {
    public static void main(String[] args) throws Exception {
        Process process = Runtime.getRuntime().exec("wmic path Win32_PnPEntity where \"Service like '%MTP%'\" get DeviceID");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line="";
        for(int i = 0;i<=2;i++){
            line = bufferedReader.readLine();
        }
        System.out.println(line);
    }
}
