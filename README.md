选题方向：经典自动化测试

所用第三方库及版本：wala 1.5.4

程序入口：entrance.class

程序结构：entrance.class作为程序入口负责处理传入的参数，Graph_generation.class根据字节码生成依赖图并储存,FindDependency根据改动方法和依赖图挑选出受影响的测试用例
