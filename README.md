<<<<<<< HEAD
1、代码clone 之后，执行：

    mvn install:install-file -Dfile=.\lib\linux\RXTXcomm.jar -DgroupId=com.hik.rxtxcomm -DartifactId=rxtxcomm-linux -Dversion=2 -Dpackaging=jar
    mvn install:install-file -Dfile=.\lib\win\RXTXcomm.jar -DgroupId=com.hik.rxtxcomm -DartifactId=rxtxcomm-win -Dversion=2 -Dpackaging=jar

2、文件复制

  #window平台:
  
        拷贝 rxtxSerial.dll —> <JAVA_HOME>\jre\bin
        拷贝 rxtxParallel.dll —> <JAVA_HOME>\jre\bin
  #linux平台:
  
        拷贝 librxtxSerial.so —> <JAVA_HOME>/jre/lib/i386/
        拷贝 librxtxParallel.so —> <JAVA_HOME>/jre/lib/i386/
