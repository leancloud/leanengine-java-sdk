# 更新日志

## 0.4.0

为了配合云函数组独立运行，LeanEngine-java-sdk 将云函数功能拆分成独立的 cloud-function-sdk，增加和修改了一些 API，主要变动如下：

* 移除：移除所有与「网站托管」相关的类和 API，包括且不限于：SessionCookie，HttpsRequestRedirect, 
* 变更：将 LeanEngine 相关的 API 由静态方法改为实例方法，使得配置和启动时的状态更可控。
  * 静态方法 LeanEngine.initialize(applicationId, clientKey, masterKey) 使用构造器 LeanEngine() 和 LeanEngine(appId, appKey, masterKey, hookKey, port, appEnv) 代替，无参数的默认构造器将从环境变量获取所有参数。
  * 静态方法 LeanEngine.register(clazz) 改为实例方法，并增加一个辅助方法可以一次注册多个类。
  * 静态方法 LeanEngine.setLocalEngineCallEnabled(enabled) 改为实例方法.
  * 静态方法 LeanEngine.setUseMasterKey(useMasterKey) 改为实例方法。
  * 静态方法 LeanEngine.getAppId() 改为实例方法。
  * 静态方法 LeanEngine.getAppKey() 改为实例方法。
  * 静态方法 LeanEngine.getMasterKey() 改为实例方法。
  * 静态方法 LeanEngine.getAppEnv() 改为实例方法。
  * 静态方法 LeanEngine.setUseMasterKey(useMasterKey) 改为实例方法。
  * 静态方法 LeanEngine.useAVCloudUS() 改为实例方法。
  * 静态方法 LeanEngine.useAVCloudCN() 改为实例方法。
* 新增：实例方法 LeanEngine.start() 和 LeanEngine.stop() 用来启动和停止云引擎服务。
