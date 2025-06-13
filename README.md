# 智慧景区无障碍 AI 助手

## 项目概述

智慧景区无障碍 AI 助手是一个面向无障碍人群的智能助手和信息平台。项目基于真实用户轨迹数据进行智能聚类分析，实现无障碍路线热力图。利用 Google Gemma3 多模态能力批量分析用户反馈的图文数据，实现地图更新。同时项目致力于构建景区无障碍社区，实现无障碍信息的共享和互助。

## 技术说明

Google Vertex AI（Gemini 大模型）承担了图片+文本的多模态智能识别、无障碍信息抽取、评论摘要等核心智能分析任务，是项目智能化的关键。备注：Vertex AI暂不支持Gemma3模型，所以本项目使用Gemini模型临时替代，后续会使用Gemma3模型。

## 技术架构与流程图


```mermaid
graph TD
    subgraph "用户交互层"
        A1["轮椅用户使用APP"] --> B1["上传GPS轨迹数据"]
        A1 --> B2["拍照上传设施/障碍物"]
        A1 --> B3["查看信息/规划路线"]
    end
    
    subgraph "AI处理层"
        C1["热力轨迹分析引擎"] 
        C2["Gemma3/Gemini多模态AI"] 
        C3["信息整合平台"]
        
        B1 --> C1
        B2 --> C2
        B3 --> C3
        
        C1 --> D1["智能聚类分析"]
        D1 --> D2["路径安全性评估"]
        D2 --> D3["热力图生成"]
        
        C2 --> E1["图像识别处理"]
        C2 --> E2["文本语义理解"]
        E1 --> E3["自动标注生成"]
        E2 --> E3
        
        C3 --> F1["用户评价整合"]
        C3 --> F2["设施信息归类"]
        F1 --> F3["社区内容管理"]
        F2 --> F3
    end
    
    subgraph "服务输出层"
        D3 --> G1["个性化导航路线推荐"]
        E3 --> G2["智能设施标注地图"]
        F3 --> G3["无障碍信息共享平台"]
        
        G1 --> H1["用户获得安全出行路线"]
        G2 --> H1
        G3 --> H1
        G3 --> H2["景区无障碍改进建议"]
    end
    
    style A1 fill:#e3f2fd,stroke:#0d47a1
    style C1 fill:#e8f5e9,stroke:#2e7d32
    style C2 fill:#fff3e0,stroke:#e65100
    style C3 fill:#f3e5f5,stroke:#6a1b9a
    style H1 fill:#e1f5fe,stroke:#01579b
    style H2 fill:#e1f5fe,stroke:#01579b
```

---

## 主要功能说明

- **无障碍信息识别**：用户上传图片和评论，系统自动识别无障碍类型及潜在问题。
- **智能摘要与查询**：对无障碍点的用户评论进行智能摘要，辅助用户查询。
- **训练与测试**：支持自定义 prompt 和图片进行 AI 能力测试。

---

## Google AI 作用与路径

- **核心作用**：Google Vertex AI（Gemini大模型）负责多模态智能识别、信息抽取、评论摘要等。
- **调用路径**：
  - Service 层（如 UpdateFeedBackService、SearchPointService、TrainingService）通过 GoogleCloudVertexAIServiceFacade 统一调用 Vertex AI。
  - GoogleCloudVertexAIServiceFacade 负责数据预处理、模型调用、结果解析。
  - GoogleVertexAIClientFactory 负责模型实例化、认证、代理等底层细节。

---

