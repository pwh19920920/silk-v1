<#assign base=request.contextPath />
<!doctype html>
<html lang="en">
<head>
    <base id="base" href="${base}">
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <link rel="stylesheet" href="${base}/main.min.css">
    <script type="text/javascript" src="${base}/jquery.min.js"></script>
    <title>用户验证</title>
    <style type="text/css">
        .LHJindex .login .logo{ height: 100px; width: 100px; display: inline-block;}
        .LHJindex .login h4{font-weight: bold; font-size: 18px}
        .loginForm{ margin: 30px auto; font-size: 14px; line-height: 38px}
        .loginForm input{border: 0 none; border-bottom: 1px solid #eee; padding: 5px 0; border-radius: 0; box-shadow: none !important;}
        .loginForm input:focus{border-color: #45bdcf;}
        .loginForm input::placeholder{color: #999; font-size: 14px}
        .loginForm .error{font-size: 12px; color: #ff1c41; line-height: 20px}
        .loginForm .getCode{position: absolute; right: 0; top: 0;  font-size: 14px}
        .loginText p{margin-bottom: 5px; font-size: 14px}
        @media (max-width:768px){
            .LHJindex .login h4{ font-size: 14px}
            .LHJindex .login .logo{ height: 70px; width: 70px;}
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
        <div class="section LHJindex">
            <div class="login text-center">
                <div class="logo"></div>
                <h4 class="mt-3">您正在使用SLB支付，请进行SilkTrader账户信息验证</h4>
                <div class="form-horizontal loginForm col-md-6 offset-3">
                    <div class="form-group mt-5 row">
                        <label for="loginphone" class="col-md-3 control-label text-left text-md-right text-sm-left">手机号：</label>
                        <div class="col-md-9">
                            <input type="text" class="form-control" id="loginphone" placeholder="请输入绑定SilkTrader的手机号">
                            <div class="error text-left hide" id='phoneErrorTip'>*<span></span></div>
                        </div>
                    </div>
                    <div class="form-group mt-5 row">
                        <label for="code" class="col-md-3 control-label text-left text-md-right text-sm-left">验证码：</label>
                        <div class="col-md-9">
                            <input type="text" class="form-control" id="code" placeholder="请输入手机验证码">
                            <button type="button" class="btn btn-link getCode" onclick="getCode(this)">获取验证码</button>
                            <div class="error text-left hide" id='codeErrorTip'>*<span></span></div>
                        </div>
                    </div>
                    <div class="form-group mt-5 row">
                        <div class="col-sm-10 offset-1">
                            <button type="button" class="btn btn-primary btn-block" id="submit">下一步</button>
                            <button type="button" class="btn btn-link mt-3" id="cancel">取消</button>
                        </div>
                    </div>
                </div>
            </div>
            <div class="mt-5 loginText text-justify">
                <p>1.什么是SLB？</p>
                <p class="ml-2" style="color: #999">SLB为SilkTrader发行的平台币，同为数字货币，总量衡定1亿枚，具备数字货币的流通价值。</p>
                <p class="ml-2" style="color: #999">SilkTrader是一家综合性数字资产交易平台，不仅为用户提供各种数字货币的实时交易，而且能提供不同线下消费场景的实时支付。</p>
                <p>2.用SilkTrader的SLB支付有哪些优势?</p>
                <p class="ml-2" style="color: #999">SLB支付像支付宝、微信一样安全、方便。</p>
                <p>3.怎么成为SilkTrader会员？</p>
                <p class="ml-2" style="color: #999">登录<a href="https://www.silktrader.net" target=_blank class="LHJlink">www.silktrader.net</a>网站即可注册成为会员。</p>
            </div>
            <form id="jump" action="${base}/customer/payInfo" method="post">
                <input type="hidden" id="orderInfo" name="orderInfo" />
                <input type="hidden" id="phone" name="phone" />
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
<script>
    $(function () {
        $('#submit').on('click',function () {
            var phone = $('#loginphone').val();
            var code = $('#code').val();
            if (phone != '' && code != '') {
                $('#phoneErrorTip').hide().find('span').html('');
                $('#codeErrorTip').hide().find('span').html('');
                var para = {
                    "phone":phone,
                    "smsCode":code,
                    "enJson":'${result.data}'
                }
                $.post("${base}/customer/phoneAuth",para,function (data) {
                    if (data.code == 0) {
                        var simpleOrder = {"sellerNick":data.data.sellerNick,"orderTime":data.data.orderTime,"orderName":data.data.orderName,"expireTime":data.data.expireTime,
                            "busi_url":'${busi_url}',"return_url":'${return_url}',
                            "sellerNick":data.data.sellerNick,"silkOrderNo":data.data.silkOrderNo,"discount":data.data.discount,"coinUnit":data.data.coinUnit,"balanceInfo":data.data.balanceInfo};
                        var so = encodeURI(JSON.stringify(simpleOrder));
                        $("#phone").val(phone);
                        $("#orderInfo").val(so);
                        var fm = $("#jump");
                        fm.submit();
                    }else {
                        alert(data.message);
                    }
                },"json");
            } else {
                if (phone == '') {
                    $('#phoneErrorTip').show().find('span').html('请输入绑定SilkTrader的手机号!');
                } else $('#phoneErrorTip').hide().find('span').html('');
                if (code == '') {
                    $('#codeErrorTip').show().find('span').html('请输入手机验证码!');
                } else $('#codeErrorTip').hide().find('span').html('');
            }
        });
        $("#cancel").on('click',function () {
            var returnUrl = '${return_url}';
            if(returnUrl == ""){
                window.location.href='${busi_url}';
            }
            window.location.href=returnUrl;
        });

    });
    function getCode(obj) {
        if($('#loginphone').val()==null || $('#loginphone').val()==''){
            alert("请填入手机号码！！！");
            return;
        }
        var time = 60;
        obj.disabled = true;
        obj.innerText = 60 + 's 重新获取';
        if(time != 60){
            return false;
        }else {
            $.post('${code_url}',{"phone":$('#loginphone').val()},function (data) {
                if (data.code == 0) {
                    alert("验证码已发送！！！");
                }else {
                    alert("验证码发送失败！！！");
                }
            },"json");
        }

        var timer = setInterval(function () {
            time --;
            obj.innerText = time + 's 重新获取';
            if (time == 0) {
                clearInterval(timer);
                obj.innerText = '获取验证码';
                obj.disabled = false;
            }
        },1000);


    }
</script>
</body>
</html>
