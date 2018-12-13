# 基于机器学习的商品类目预测

## 项目介绍

项目根据商品名称把商品预测出商品所属的类目。本项目中商品类目分为三级，一共有962的三级类目，完整的类目文件见：[src/main/resources/category.json](src/main/resources/category.json)。比如名称为“荣耀手机原装华为p9p10plus/mate10/9/8/nova2s/3e荣耀9i/v10手机耳机【线控带麦】AM115标配版白色”的商品，该商品的实际类目是【手机耳机】，我们需要训练一个模型能够根据商品名称自动预测出该商品属于【手机耳机】类目。

项目基于JAVA语言开发，使用Spring Boot开发框架和Spark MLlib机器学习框架，以RESTful接口的方式对外提供服务。

## 软件架构

该项目属于千米网Ocean大数据平台的一个子项目。

项目的架构如下：

![架构图](http://ww1.sinaimg.cn/mw690/44608603gy1fy438e5x7yj21tc0mc76t.jpg)

Ocean大数据平台的架构如下：

![Ocean架构图](http://ww1.sinaimg.cn/large/44608603gy1fy53tf2g4sj20ty0vetb7.jpg)

## 算法和设计

商品类目预测，归根结底是一个分类问题，一个有监控的机器学习问题。每个标准类目可以看成是一个分类，程序需要自动把商品划分到各个分类中。项目主要使用TF-IDF和Bayes算法。TF-IDF用于商品特征向量的提取和优化，Bayes用于对商品特征向量的分类。

### 特征工程

特征工程完成商品名称文本到向量的转化。

#### 1. 商品名称分词

商品名称我们需要通过分词将其切分成相互独立的词条。中文分词方法主要有两类：基于词典和基于统计。

中文分词算法尝试了IK、Ansj和结巴，测试结果显示在商品领域结巴分词效果最好。

#### 2. 自定义词典

针对商品这一特定领域，收集和整理了电商领域的专业词汇，经过筛选和去重后，生成了自定义词典，词典包含30万词汇，词典文件见：[/data/product.dict](/data/product.dict)。最后商品名称分词方法为结巴分词+30万词汇的自定义词典。

#### 3. 商品名称特征向量

将商品名称分词后形成词条，如果所有商品的词条有10万个，那么就可以将每个商品名称转换为一个10万维空间的向量。

假设只存在下面的两个商品：

* 商品1： 澳贝 AUBY 森林 钢琴 AUBY  

* 商品2： 澳贝 AUBY 益智玩具 花篮  

那么商品的总词条为：[澳贝 AUBY 森林 钢琴 益智玩具 花篮]，向量的维度为总词条的数目，向量的值为该商品中对应词条的数目，则商品名称向量为：

* 商品1： [1, 2, 1, 1, 0, 0]  

* 商品2： [1, 1, 0, 0, 1, 1]

#### 4. 商品特征向量优化

上述特征向量的维度为总词条的数目，当样本很大的情况下，词条可能会达到10万甚至百万级，向量的维度太高导致：

1. 计算的复杂度指数级增加；

2. 稀疏矩阵对计算的准确性也有影响。

因此需要Hash算法对词条向量进行降维处理。

上节向量的值只考虑到词条的数目，没有考虑到词条的权重。比如一个商品标题分词后的结果为“品胜 iphone 苹果 手机 耳机”，显然“耳机”词条的权重或者区分度应该最高。如何找出这样区分度或者权重最高的词呢？可以通过[TF-IDF（term frequency–inverse document frequency）](https://en.wikipedia.org/wiki/Tf%E2%80%93idf)算法进行处理。

### 模型训练

以预先标注好类目的商品数据作为训练样本，使用Bayes算法对向量进行分类，训练出一个商品类目分类模型，后续就可以使用该模型对商品类目进行预测。

### 模型评测

将训练数据随机分成9:1两份，90%的数据用于模型训练，10%的数据用于评测，测试结果显示类目预测准确率为82%。

因为商品信息只有名称，如果有更多的信息（品牌、价格、厂家等），预测准确率会进一步提高。

## 安装教程

### 1. 安装并运行Spark

项目使用的Spark版本为2.2.1，详情见[Spark安装使用说明](https://spark.apache.org/docs/2.2.1/spark-standalone.html)。

### 2. 下载源码

``` shell
  $ cd  {predict_project_home}
  $ git clone https://github.com/jingpeicomp/product-category-predict.git
```

### 3. 修改参数配置

项目的配置文件见 {predict_project_home}/src/main/java/resources/application.properties。

配置名 | 值 |  说明
:----------- | :-----------| :-----------
server.port     | 默认值8082    | Web应用对外服务端口
category.dataPath    | {predict_project_home}/data         | 数据文件目录路径，包含商品名称分词自定义词典product.dict、训练数据
category.modelPath    | {predict_project_home}/model         | 机器学习模型文件目录路径
category.spark.masterUrl     | 如果是Spark单Standalone安装方式的话，默认地址是spark://localhost:7077    | spark集群master url
category.spark.dependenceJar     | {predict_project_home}/target/product-category-predict-1.0.0-SNAPSHOT-jar-with-dependencies.jar    | Spark App 依赖的jar文件
category.spark.properties.****     | 无    | 以category.spark.properties.开头的属性都是Spark配置参数，最终都会设置到Spark App上。不同Spark部署方式对应的属性不同，详情见[Spark配置参数说明](https://spark.apache.org/docs/2.2.1/configuration.html)。[现有的配置文件](src/main/java/resources/application.properties)是针对Standalone部署方式的参数。Spark最重要的配置参数是CPU和内存资源的设定。

### 4. 通过maven打包

```shell
  $ cd  {predict_project_home}
  $ mvn clean package -Dmaven.test.skip=true
```

项目使用了jar-with-dependencies和Spring Boot打包插件，最后在目录 {predict_project_home}/target 生成三个jar文件：

* original-product-category-predict-1.0.0-SNAPSHOT.jar是项目源码jar；

* product-category-predict-1.0.0-SNAPSHOT-jar-with-dependencies.jar是包含了所有依赖jar，作为Spark应用的依赖jar，提交到Spark集群上；

* product-category-predict-1.0.0-SNAPSHOT.jar是Spring Boot可运行jar；

## 使用说明

### 启动应用

```shell
  $ cd  {predict_project_home}
  $ chmod a+x target/product-category-predict-1.0.0-SNAPSHOT.jar
  $ java -jar target/product-category-predict-1.0.0-SNAPSHOT.jar
```

由于项目基于Spring Boot框架，因此Spring Boot所有的启动参数都适用于本项目。

出现如下日志则代表应用启动成功：

```log
---------------------------------
Finish to start application !
---------------------------------
```

### 启动模型训练

项目启动时会判断[application.properties](src/main/java/resources/application.properties)中`category.modelPath`参数配置的模型文件目录是否存在模型，如果没有模型，则会启动模型的训练。

训练时长和训练样本大小、物理资源相关。在2.2G I7 CPU的MacbookPro(2014)笔记本电脑上，项目的Spark应用分配的资源为8核CPU、8G内存，训练样本为1200万商品数据，大概需要10分钟。

### 模型和样本数据

#### 模型数据

项目[model](/model)目录已经附上了我本地训练好的一个模型，可以直接用来预测。

#### 训练样本

由于商业原因，训练数据不能公开。项目附上了一个简单的测试训练样本[data/train.data](/train/data)，有20万商品数据，可以用来测试、训练。因为样本较小的关系，训练出来的模型的准确率会很低。

训练数据的一行表示一个商品，格式为`{三级类目ID} |&| {商品名称} |&| {商品名称分词结果}`。

```text
0.0 |&| 耐尔金 摩托罗拉moto Z Play/XT1635 防爆钢化玻璃膜/手机保护贴膜 H+pro弧边0.2mm |&| 耐尔 摩托罗拉 moto Play XT1635 防爆 钢化玻璃 手机 保护 贴膜 pro 弧边 mm
0.0 |&| 戴星 手机钢化玻璃膜防爆高清保护膜 适用于魅族 魅蓝2/魅蓝M2 弧边-钢化膜 |&| 戴星 手机 钢化玻璃 防爆 高
清 保护膜 适用 魅族 魅蓝 魅蓝 M2 弧边 钢化
934.0 |&| 多丽丝巴比公主会说话的智能对话娃娃仿真洋娃娃对话关节女孩玩具 限量静态-圣诞公主 |&| 丽丝 巴比 公主 说话 智能 对话 娃娃 仿真 洋娃娃 对话 关节 女孩 玩具 限量 静态 圣诞 公主
```

### RESTful接口

#### 1. 查询商品标准类目

| URL        | HTTP           | 功能  |  
| ------------- |-------------| -----|  
| /api/categories      | GET | 返回所有类目条目 |

##### 请求参数

无

##### 请求结果

> http://localhost:8082/api/categories

```json

[
    {
        "thirdCateId": 0, //三级类目ID
        "thirdCate": "手机贴膜", //三级类目名称
        "secondCateId": 0, //二级类目ID
        "secondCate": "手机配件", //二级类目名称
        "firstCateId": 0, //一级类目ID
        "firstCate": "手机" //一级类目名称
    },
    {}
]

```

#### 2. 查询商品名称分词结果

| URL        | HTTP           | 功能  |  
| ------------- |-------------| -----|  
| /api/categories/segment      | GET | 返回商品名称分词结果 |

##### 请求参数

| 参数名        | 数据类型           | 可需  |   描述 |  
| ------------- |-------------| -----|  ---------|  
| name | string | 必填 | 待分词的商品名称 |

##### 请求结果

> http://localhost:8082/api/categories/segment?name=荣耀手机原装华为p9p10plus/mate10/9/8/nova2s/3e荣耀9i/v10手机耳机【线控带麦】AM115标配版白色

```json
荣耀 手机 原装 华为 p9p10plus mate10 nova2s 荣耀 v10 手机 耳机 线控 带麦 am115 标配 白色
```

#### 3. 预测商品类目

| URL        | HTTP           | 功能  |  
| ------------- |-------------| -----|  
| /api/categories/predict      | GET | 预测商品类目 |

##### 请求参数

| 参数名        | 数据类型           | 可需  |   描述 |  
| ------------- |-------------| -----|  ---------|  
| names | string | 必填 | 商品名称，多个商品名称用","分隔 |

##### 请求结果

> http://localhost:8082/api/categories/predict?names=荣耀手机原装华为p9p10plus/mate10/9/8/nova2s/3e荣耀9i/v10手机耳机【线控带麦】AM115标配版白色,调味酱日本原装进口神州一小美子米味噌汤日式味增酱料1kg包邮,印尼进口Tango咔咔脆巧克力夹心威化饼干160g*3休闲网红零食年货

```json
[
    {
        "thirdCateId": 3,
        "thirdCate": "手机耳机",
        "secondCateId": 0,
        "secondCate": "手机配件",
        "firstCateId": 0,
        "firstCate": "手机",
        "productName": "荣耀手机原装华为p9p10plus/mate10/9/8/nova2s/3e荣耀9i/v10手机耳机【线控带麦】AM115标配版白色"
    },
    {
        "thirdCateId": 622,
        "thirdCate": "粮油调味",
        "secondCateId": 62,
        "secondCate": "进口食品",
        "firstCateId": 12,
        "firstCate": "食品饮料、保健食品",
        "productName": "调味酱日本原装进口神州一小美子米味噌汤日式味增酱料1kg包邮"
    },
    {
        "thirdCateId": 615,
        "thirdCate": "休闲零食",
        "secondCateId": 61,
        "secondCate": "休闲食品",
        "firstCateId": 12,
        "firstCate": "食品饮料、保健食品",
        "productName": "印尼进口Tango咔咔脆巧克力夹心威化饼干160g*3休闲网红零食年货"
    }
]
```

### 结果示例

比如名称中包含“小米”的商品，可能是小米品牌数目相关的商品，也有可能是粮食。下面是几个商品的测试结果：

| 商品名称        | 预测类目           |  
| ------------- |-------------|
| 小米 Max 全网通 3GB内存 64GB ROM 金色 移动联通电信4G手机 | 手机 |
| 小米 Max 全网通 3GB内存 64GB ROM 金色 移动联通电信4G | 手机 |
| 小米 Max 全网通 3GB内存 金色 | 手机 |
| 小米 Max 全网通 | 手机 |
| 裕道府 红谷黄小米 健康五谷杂粮 东北谷子100g | 米面杂粮 |
| 红谷黄小米 健康五谷杂粮 东北谷子 | 米面杂粮 |
| 黄小米 健康五谷杂粮 | 米面杂粮 |
| 小米 健康杂粮 | 米面杂粮 |
| 小米 健康 | 智能手环 |