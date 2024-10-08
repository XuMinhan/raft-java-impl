# raft 的 java 实现

#### 使用 netty 作为网络框架，结合opaque实现异步 / 同步 / 回调

#### 使用 kryo 作为序列化框架，用于 持久化 和 网络传输

未经过完善测试，个人学习玩具

参考文章

* raft简略讲解 https://cloud.tencent.com/developer/article/2168468
* raft论文 https://pdos.csail.mit.edu/6.824/papers/raft-extended.pdf

目前实现

0. [x] 网络基架搭建
1. [x] 序列化基架搭建
2. [x] 心跳检测
3. [x] 过期选举
4. [ ] 日志复制