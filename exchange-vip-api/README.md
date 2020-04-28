# 星客 API-Docs
[星客交易所](https://www.silktraderdk.net)<br>

关于apikey申请和修改，请在“账户 - API管理”页面进行相关操作。

支持所有**https://www.silktraderdk.net**中的交易对

常见问题请参考[FAQ](http://172.16.0.99/SilkTraderManager/silktrader-platform/wikis/home)

## REST行情、交易API<br>
* [REST API简介](http://172.16.0.99/SilkTraderManager/silktrader-platform/wikis/rest-api-introduction)<br>
* [签名认证(重要，请仔细阅读)](http://172.16.0.99/SilkTraderManager/silktrader-platform/wikis/signature-authentication)<br>
* [请求说明(一定要看)](http://172.16.0.99/SilkTraderManager/silktrader-platform/wikis/rest-request-instructions)<br>
* [API Reference](http://172.16.0.99/SilkTraderManager/silktrader-platform/wikis/API-Reference)<br>
* 代码示例 [Java](http://172.16.0.99/silktrade-openapi/Rest-Java-Demo) 

## API相关说明

* 接口包括全部行情接口以及如下需要验签的接口

接口|说明|
----------------------|---------------------|
[POST /v3/order/orders/place](http://172.16.0.99/SilkTraderManager/silktrader-platform/wikis/API-Reference)	|创建委托订单|
[POST /v3/order/orders/batchplace](http://172.16.0.99/SilkTraderManager/silktrader-platform/wikis/API-Reference)	|批量创建委托订单|
[POST /v3/order/orders/{orderId}/submitcancel](http://172.16.0.99/SilkTraderManager/silktrader-platform/wikis/API-Reference)	|撤销委托订单|
[POST /v3/order/orders/batchcancel](http://172.16.0.99/SilkTraderManager/silktrader-platform/wikis/API-Reference)	|批量撤销订单|
[GET /v3/order/orders/{orderId}](http://172.16.0.99/SilkTraderManager/silktrader-platform/wikis/API-Reference)	|查询一个订单详情|
[GET /v3/order/orders](http://172.16.0.99/SilkTraderManager/silktrader-platform/wikis/API-Reference)	|查询历史委托|
[GET /v3/order/openOrders](http://172.16.0.99/SilkTraderManager/silktrader-platform/wikis/API-Reference)	|查询当前委托订单|
[GET /v3/order/orders/{order-id}/matchresults](http://172.16.0.99/SilkTraderManager/silktrader-platform/wikis/API-Reference)	|查询某个订单的成交明细|
[GET /v3/account/contract_account_info](http://172.16.0.99/SilkTraderManager/silktrader-platform/wikis/API-Reference)	|查询某个币种的账户信息|

