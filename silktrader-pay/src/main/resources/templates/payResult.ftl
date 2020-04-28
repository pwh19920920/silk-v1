<#assign base=request.contextPath />
<!doctype html>
<html lang="en">
<head>
    <base id="base" href="${base}">
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <link rel="stylesheet" href="${base}/main.min.css">
    <title>支付结果</title>
    <style type="text/css">
        .payStatus{margin: 60px 0 80px 0;}
        .payStatus .status{display: block; width: 120px;height: 90px;margin: 0 auto; border-radius: 50%; background: no-repeat top center;  font-size: 20px; font-weight: bold; padding-top: 110px; text-align: center}
        .payStatus .status.success{ background-image:url('data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBzdGFuZGFsb25lPSJubyI/PjwhRE9DVFlQRSBzdmcgUFVCTElDICItLy9XM0MvL0RURCBTVkcgMS4xLy9FTiIgImh0dHA6Ly93d3cudzMub3JnL0dyYXBoaWNzL1NWRy8xLjEvRFREL3N2ZzExLmR0ZCI+PHN2ZyB0PSIxNTMxNTMxMDcwNjE5IiBjbGFzcz0iaWNvbiIgc3R5bGU9IiIgdmlld0JveD0iMCAwIDEwMjUgMTAyNCIgdmVyc2lvbj0iMS4xIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHAtaWQ9Ijc5MTAiIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hsaW5rIiB3aWR0aD0iMTAwLjA5NzY1NjI1IiBoZWlnaHQ9IjEwMCI+PGRlZnM+PHN0eWxlIHR5cGU9InRleHQvY3NzIj48L3N0eWxlPjwvZGVmcz48cGF0aCBkPSJNNTExLjk3NDQwMSAwYy0yODIuNzU1MjcgMC01MTEuOTc0NDAxIDIyOS4yMTkxMzEtNTExLjk3NDQwMSA1MTEuOTc0NDAxIDAgMjgyLjc1NzMxOCAyMjkuMjE5MTMxIDUxMS45NzQ0MDEgNTExLjk3NDQwMSA1MTEuOTc0NDAxIDI4Mi43NTczMTggMCA1MTEuOTc0NDAxLTIyOS4yMTcwODMgNTExLjk3NDQwMS01MTEuOTc0NDAxQzEwMjMuOTQ4ODAzIDIyOS4yMTkxMzEgNzk0LjcyOTY3MiAwIDUxMS45NzQ0MDEgMHpNODA1LjYzMDYzIDM3OS4xNzQzODUgNDc0LjUxMDE2MiA3MTAuMjk2OTAxYzAgMC0wLjAwNDA5NiAwLjAwNDA5Ni0wLjAxMDIzOSAwLjAxMDIzOS0xNS4yNjUwMjkgMTUuMjY5MTI1LTM4LjU0MTQzMyAxNy42NTI4NzctNTYuMzExMDQgNy4xNTc0MDItMy4yOTA5NzEtMS45NDU1MDMtNi4zOTM1MzYtNC4zMzMzNTEtOS4yMTk2MzUtNy4xNTc0MDItMC4wMDIwNDgtMC4wMDQwOTYtMC4wMDYxNDQtMC4wMDYxNDQtMC4wMDYxNDQtMC4wMDYxNDRsLTE5MC42NDI4ODQtMTkwLjY0Mjg4NGMtMTguMDk1MjIzLTE4LjA5NTIyMy0xOC4wOTUyMjMtNDcuNDM3NSAwLTY1LjUzNjgxOSAxOC4wOTUyMjMtMTguMDk1MjIzIDQ3LjQzNzUtMTguMDk1MjIzIDY1LjUzMjcyMyAwbDE1Ny44ODQ3MTQgMTU3Ljg4NDcxNCAyOTguMzYyMjk4LTI5OC4zNjIyOThjMTguMDk3MjcxLTE4LjA5NTIyMyA0Ny40Mzk1NDgtMTguMDk1MjIzIDY1LjUzNDc3MSAwQzgyMy43MjU4NTQgMzMxLjczODkzMyA4MjMuNzI1ODU0IDM2MS4wNzkxNjIgODA1LjYzMDYzIDM3OS4xNzQzODV6IiBwLWlkPSI3OTExIiBmaWxsPSIjNDViZGNmIj48L3BhdGg+PC9zdmc+');}
        .payStatus .status.fail{ background-image:url('data:image/svg+xml;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBzdGFuZGFsb25lPSJubyI/PjwhRE9DVFlQRSBzdmcgUFVCTElDICItLy9XM0MvL0RURCBTVkcgMS4xLy9FTiIgImh0dHA6Ly93d3cudzMub3JnL0dyYXBoaWNzL1NWRy8xLjEvRFREL3N2ZzExLmR0ZCI+PHN2ZyB0PSIxNTMxNTMwNTgxNjY0IiBjbGFzcz0iaWNvbiIgc3R5bGU9IiIgdmlld0JveD0iMCAwIDEwMjQgMTAyNCIgdmVyc2lvbj0iMS4xIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHAtaWQ9IjUwNDkiIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hsaW5rIiB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCI+PGRlZnM+PHN0eWxlIHR5cGU9InRleHQvY3NzIj48L3N0eWxlPjwvZGVmcz48cGF0aCBkPSJNODc1LjQzMjEzNyAxNDguNTY3ODYzYy0xOTguMDkwNDg0LTE5OC4wOTA0ODQtNTIzLjk4MTI3OS0xOTguMDkwNDg0LTcyMi4wNzE3NjMgMHMtMTk4LjA5MDQ4NCA1MjMuOTgxMjc5IDAgNzIyLjA3MTc2MyA1MjMuOTgxMjc5IDE5OC4wOTA0ODQgNzIyLjA3MTc2MyAwUzEwNzMuNTIyNjIxIDM1My4wNDgzNjIgODc1LjQzMjEzNyAxNDguNTY3ODYzek02NzAuOTUxNjM4IDcxNy4yNzkyNTEgNTExLjIwMTI0OCA1NTcuNTI4ODYxbC0xNTkuNzUwMzkgMTU5Ljc1MDM5Yy0xMi43ODAwMzEgMTIuNzgwMDMxLTMxLjk1MDA3OCAxMi43ODAwMzEtNDQuNzMwMTA5IDAtMTIuNzgwMDMxLTEyLjc4MDAzMS0xMi43ODAwMzEtMzEuOTUwMDc4IDAtNDQuNzMwMTA5TDQ2Ni40NzExMzkgNTEyLjc5ODc1MiAzMDYuNzIwNzQ5IDM1My4wNDgzNjJDMjkzLjk0MDcxOCAzNDAuMjY4MzMxIDI5My45NDA3MTggMzIxLjA5ODI4NCAzMDYuNzIwNzQ5IDMwOC4zMTgyNTNjMTIuNzgwMDMxLTEyLjc4MDAzMSAzMS45NTAwNzgtMTIuNzgwMDMxIDQ0LjczMDEwOSAwTDUxMS4yMDEyNDggNDY4LjA2ODY0M2wxNTkuNzUwMzktMTU5Ljc1MDM5YzEyLjc4MDAzMS0xMi43ODAwMzEgMzEuOTUwMDc4LTEyLjc4MDAzMSA0NC43MzAxMDkgMCAxMi43ODAwMzEgMTIuNzgwMDMxIDEyLjc4MDAzMSAzMS45NTAwNzggMCA0NC43MzAxMDlMNTU1LjkzMTM1NyA1MTIuNzk4NzUybDE1OS43NTAzOSAxNTkuNzUwMzljMTIuNzgwMDMxIDEyLjc4MDAzMSAxMi43ODAwMzEgMzEuOTUwMDc4IDAgNDQuNzMwMTA5UzY4My43MzE2NjkgNzMwLjA1OTI4MiA2NzAuOTUxNjM4IDcxNy4yNzkyNTF6IiBwLWlkPSI1MDUwIiBmaWxsPSIjNDViZGNmIj48L3BhdGg+PC9zdmc+');;}
        .payInfo{background: #f5f5f5; padding: 40px 60px;}
        .payInfo h2{font-weight: bold; font-size: 20px; margin-bottom: 30px;}
        .payInfo .item{font-size: 14px; line-height: 1.2;}
        .payInfo .item div{overflow: hidden;}
        .payInfo .item .name{color: #999; float: left; width: 85px; text-align: justify;}
        .payInfo .item .name:after {content: "";display: inline-block;width: 100%;line-height: 0;}
        .payInfo .item em{font-weight: bold; font-style: normal; color: #ff1c41}
        @media (max-width:768px){
            .payStatus{margin: 20px 0 60px 0;}
            .payStatus .status{height: 60px; padding-top: 60px; background-size: 50px 50px}
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
            <div style="color: #999; font-size: 12px">【已支付】<!--支付失败【已取消】--></div>
            <div class="payStatus text-center">
                <span class="status success">SLB支付成功</span>
                <!--支付失败
                <span class="status fail">SLB支付失败</span>
                -->
            </div>
            <div class="payInfo col-md-10 offset-md-1">
                <h2 class="text-center">支付信息</h2>
                <div class="row item">
                    <div class="col-md-12"><span class="name">商家名称：</span>${result.data.sellerNick}</div>
                    <div class="col-md-6"><span class="name">订单号：</span>${result.data.orderId}</div>
                    <div class="col-md-6"><span class="name">交易流水号：</span>${result.data.silkOrderNo}</div>
                    <div class="col-md-6"><span class="name">订单名称：</span>${result.data.orderName}</div>
                    <div class="col-md-6"><span class="name">SLB实时价：</span>${result.data.contractBusiPrice * result.data.busiCurrencyPrice}  CNY/${result.data.coinUnit}</div>
                    <div class="col-md-6"><span class="name">下单时间：</span>${result.data.orderTime}</div>
                    <div class="col-md-6"><span class="name">折扣率：</span>${result.data.discount}     </div>
                    <div class="col-md-6"><span class="name">订单金额：</span>${result.data.amount}元</div>
                    <div class="col-md-6"><span class="name">应支付：</span><em>${result.data.contractAmount} </em> ${result.data.coinUnit}</div>
                </div>
            </div>
            <div class="text-center">
                <P><button type="button" class="btn btn-primary mt-5" id="cancel" style="width: 300px">返回</button></P>
                <!--支付失败
                <P><button type="button" class="btn btn-primary mt-5" style="width: 300px">重新支付</button></P>
                <P><button type="button" class="btn btn-link" style="width: 300px">返回</button></P>
                -->
            </div>
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
        $("#cancel").on('click',function () {
            var returnUrl = '${return_url}';
            if(returnUrl == ""){
                window.location.href='${busi_url}';
            }
            window.location.href=returnUrl;
        });
    });
</script>
</body>
</html>
