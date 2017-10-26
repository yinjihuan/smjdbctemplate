package com.cxytiandi.jdbc.keygen;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 根据机器IP获取工作进程Id,如果线上机器的IP二进制表示的最后10位不重复,建议使用此种方式
 * ,列如机器的IP为192.168.1.108,二进制表示:11000000 10101000 00000001 01101100
 * ,截取最后10位 01 01101100,转为十进制364,设置workerId为364.
 *
 * @author DonneyYoung
 */
public final class IPKeyGenerator implements KeyGenerator {
    
    private final DefaultKeyGenerator defaultKeyGenerator = new DefaultKeyGenerator();
    
    static {
        initWorkerId();
    }
    
    static void initWorkerId() {
        InetAddress address;
        try {
            address = InetAddress.getLocalHost();
        } catch (final UnknownHostException e) {
            throw new IllegalStateException("Cannot get LocalHost InetAddress, please check your network!");
        }
        byte[] ipAddressByteArray = address.getAddress();
        DefaultKeyGenerator.setWorkerId((long) (((ipAddressByteArray[ipAddressByteArray.length - 2] & 0B11) << Byte.SIZE) + (ipAddressByteArray[ipAddressByteArray.length - 1] & 0xFF)));
    }
    
    @Override
    public Number generateKey() {
        return defaultKeyGenerator.generateKey();
    }
}