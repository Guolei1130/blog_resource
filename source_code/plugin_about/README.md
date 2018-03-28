
所涉及到的代码全部通过Nexus 5,Android6.0 Dalvik虚拟机测试

plugindemo-release-unsigned.apk 为测试包，plugin_1.apk,plugin_so.apk为插件包，simpledex.jar为dex文件。

adb install -t 测试包
adb push 插件包 /sdcard/
adb push dex文件 /sdcard/