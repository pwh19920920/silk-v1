<#assign base=request.contextPath />
<!doctype html>
<html lang="en">
<head>
    <base id="base" href="${base}">
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <link rel="stylesheet" href="${base}/main.min.css">
    <title>支付</title>
    <style type="text/css">
        .payTip{font-size:20px; font-weight: bold; margin: 60px 0}
        .payTip span{color: #ff1c41}
        .payInfo{background: #f5f5f5; padding: 40px 60px;}
        .payInfo h2{font-weight: bold; font-size: 20px; margin-bottom: 30px;}
        .payInfo .item{font-size: 14px; line-height: 1.2;}
        .payInfo .item div{overflow: hidden;}
        .payInfo .item .name{color: #999; float: left; width: 85px; text-align: justify;}
        .payInfo .item .name:after {content: "";display: inline-block;width: 100%;line-height: 0;}
        .payInfo .item em{font-weight: bold; font-style: normal; color: #ff1c41}
        .loginForm{ margin: 30px auto; font-size: 14px; line-height: 38px}
        .loginForm input{border: 0 none; border-bottom: 1px solid #eee; padding: 5px 0; border-radius: 0; box-shadow: none !important;}
        .loginForm input:focus{border-color: #45bdcf;}
        .loginForm input::placeholder{color: #999; font-size: 14px}
        .loginForm .error{font-size: 12px; color: #ff1c41; line-height: 20px}
        .loginForm .getCode{position: absolute; right: 15px; top: 0;  font-size: 14px}
        @media (max-width:768px){
            .payTip{margin: 20px 0; font-size: 14px}
            .payInfo{padding: 20px 15px;}
            .LHJsection .container .section{padding:20px 0px}
        }
    </style>
</head>
<body>
<div class="LHJheader">
    <div class="container">
        <div class="logo float-left">—— 全球优质数字资产交易平台</div>
    </div>
</div>
<div class="LHJsection">
    <div class="container">
        <div class="section">
            <div style="color: #999; font-size: 12px">【待支付】</div>
            <div id="eptime" class="payTip text-center">请您在<span></span>内完成支付，否则将自动取消交易！</div>
            <div class="payInfo col-md-10 offset-md-1">
                <h2 class="text-center">支付信息</h2>
                <div class="row item">
                    <input id="orderInfo" type="hidden" value="${orderInfo}" />
                    <div class="col-md-12"><span class="name">商家名称：</span>${result.data.sellerNick}</div>
                    <div class="col-md-6"><span class="name">订单号：</span>${result.data.orderId}</div>
                    <div class="col-md-6"><span class="name">交易流水号：</span>${result.data.silkOrderNo}</div>
                    <div class="col-md-6"><span class="name">订单名称：</span>${result.data.orderName}</div>
                    <div class="col-md-6"><span class="name">SLB实时价：</span>${result.data.contractBusiPrice * result.data.busiCurrencyPrice}  CNY/${result.data.coinUnit}</div>
                    <div class="col-md-6"><span class="name">下单时间：</span>${result.data.orderTime}</div>
                    <div class="col-md-6"><span class="name">折扣率：</span>${result.data.discount}     </div>
                    <div class="col-md-6"><span class="name">订单金额：</span>${result.data.amount}元</div>
                    <div class="col-md-6"><span class="name">应支付：</span><em>${result.data.contractAmount} </em> ${result.data.coinUnit}（${result.data.balanceInfo}<a href="https://www.silktrader.net/#/finance/recharge?name=SLB" target="_blank" class="LHJlink">去充值</a>）</div>
                </div>
            </div>
            <form id="jump" method="post" onsubmit="return false" class="form-horizontal loginForm col-md-6 offset-md-3">
                <input type="hidden" id="simpleOrder" name="simpleOrder" />
                <input type="hidden" id="busiUrl" name="busiUrl" />
                <input type="hidden" id="returnUrl" name="returnUrl" />
                <input type="hidden" id="code" name="code" />
                <div class="form-group mt-5 row">
                    <label for="fundsPassword" class="col-md-3 control-label text-left text-md-right text-sm-left">资金密码：</label>
                    <div class="col-md-9">
                        <input type="password" class="form-control" name="fundsPassword" id="fundsPassword" autocomplete="off" value="" placeholder="请输入SilkTrader的资金密码">
                        <a href="https://www.silktrader.net/#/findPwd" target="_blank" class="LHJlink getCode">忘记密码？</a>
                        <div class="error text-left hide" id="errorTip">*<span></span></div>
                        <div class="text-center">
                            <button type="button" class="btn btn-primary btn-block mt-5" id="pay">立即支付</button>
                            <button type="button" class="btn btn-link mt-3" id="cancel">取消</button>
                        </div>
                    </div>
                </div>
            </form>
        </div>
    </div>
</div>
<div class="LHJfooter">
    <div class="container">
        <div class="logo"></div>
        <div class="item">
            <p>全球优质数字资产交易平台</p>
            <p>
                <span class="float-left">Copyright © 2018 - 2020</span>
                <span class="float-right">客服邮箱：service@silktrader.net</span>
            </p>
        </div>
    </div>
</div>
<script type="text/javascript" src="${base}/jquery.min.js"></script>
<script type="text/javascript" src="${base}/md5.min.js"></script>
<script>
    $(function () {
        $('#pay').on('click',function () {
            var pwd = $('#fundsPassword').val();
            var md5pwd = md5(pwd + 'hello, moto');
            var orderInfo= encodeURI($("#orderInfo").val());//进行url编码
            if (pwd != '' && pwd != null) {
                if(pwd.length<6){
                    $('#errorTip').show().find('span').html('请输入至少6位数长度的密码!')
                    return;
                }
                var para = {
                    "phone":${phone},
                    "jypwd":md5pwd,
                    "orderInfo":orderInfo
                }

                $.post("${base}/customer/pwdAuth",para,function (data) {
                    var simpleOrder = encodeURI(JSON.stringify(${resultForString}));
                    var busiUrl = '${busi_url}';
                    var returnUrl = '${return_url}';
                    if (data.code == 0) {//用户资金密码验证成功，跳转到支付结果页面
                        $("#simpleOrder").val(simpleOrder);
                        $("#busiUrl").val(busiUrl);
                        $("#returnUrl").val(returnUrl);
                        var fm = $("#jump");
                        fm.attr("action","${base}/customer/coinPay");
                        fm.attr("onsubmit","true");
                        fm.submit();
                    }else {
                        $("#simpleOrder").val(simpleOrder);
                        $("#busiUrl").val(busiUrl);
                        $("#returnUrl").val(returnUrl);
                        $("#code").val(data.code);
                        var fm = $("#jump");
                        fm.attr("action","${base}/customer/coinPayFail");
                        fm.attr("onsubmit","true");
                        fm.submit();
                    }
                },"json")
            } else {
                $('#errorTip').show().find('span').html('请填写资金密码!')
            }
        });
        if(${expireTime}==-1){
            $("#eptime").html("");
        }else {
            //每秒循环一次，刷新时间
            id = setInterval(setTimer, 1000);
        }
        if (plus <= 0 && plus!=-1) {
            $("#eptime").html("支付已超时，已自动取消交易！");
        }else {
            $("#eptime span").html(minute + "分" + second + "秒")
        }
        $("#cancel").on('click',function () {
            var returnUrl = '${return_url}';
            if(returnUrl == ""){
                window.location.href='${busi_url}';
            }
            window.location.href=returnUrl;
        });
    });
    var id;
    var plus =${expireTime}*1000;
    var minute = parseInt(plus / 1000 / 60) - parseInt(plus / 1000 / 60 / 60) * 60;
    var second = parseInt(plus / 1000) - parseInt(plus / 1000 / 60) * 60;
    //倒计时
    function setTimer() {
        plus -= 1000;
        minute = parseInt(plus / 1000 / 60) - parseInt(plus / 1000 / 60 / 60) * 60;
        second = parseInt(plus / 1000) - parseInt(plus / 1000 / 60) * 60;
        minute = checkTime(minute);
        second = checkTime(second);

//        document.getElementById("eptime span").innerHTML =  minute + "分" + second + "秒";
        $("#eptime span").html(minute + "分" + second + "秒");

        if (plus <= 1 && plus !=-1) {
            $("#eptime").html("支付已超时，已自动取消交易！");
            clearInterval(id);
        }
    }

    function checkTime(i) {
        if (i < 10) {
            i = "0" + i;
        }
        return i;
    }

</script>
</body>
</html>
