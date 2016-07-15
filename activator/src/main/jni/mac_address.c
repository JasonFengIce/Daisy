//
// Created by huibin on 6/2/16.
//

#include "mac_address.h"
#include <stdio.h>
#include <memory.h>

char *get_mac_address() {
    char *filename = "sys/class/net/eth0/address";
    char ch;
    char macaddress[64];
    FILE *fp;
    int i = 0;
    memset(macaddress, 0, 64);
    char *buf = (char *) malloc(64);
    if ((fp = fopen(filename, "r")) == NULL) {
//        LOGVFINGERPRINT("不能打开文件\n");
        memset(buf, 0, 64);
        memcpy(buf, "noaddress", strlen("noaddress"));
    } else {
        while ((ch = fgetc(fp)) != EOF) {
            if (ch >= 255)
                break;
            macaddress[i++] = ch;
        }
//        LOGVFINGERPRINT("macaddress = %s", macaddress);
        memset(buf, 0, 64);
        memcpy(buf, macaddress, strlen(macaddress));
        fclose(fp);
    }

    return buf;
}
