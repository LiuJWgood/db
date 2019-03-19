//1. 从当前页面, 获取到用户输入内容
// 如何从url路径上获取请求参数: js
var kv = window.location.search;
//如果没有传递任何的参数跳转首页
if (kv == null || kv == "") {
    window.location.href = "/index.html";

}
var v = kv.split("=")[1];

//2. 将数据再次回显给搜索框
//回显之前, 需要将数据, 进行url解码
//如果想做全部替换, 需要使用正则表达式


v = decodeURI(v);
//   /\+/g :  是一个正则对象
v =  v.replace(/\+/g," ");
v =  v.replace(/%2B/g,"+");

$("#inputSeach").val(v);

//3. 发送异步请求
ajaxQuery("1", "15");


function ajaxQuery(page, pageSize) {
    //数据清空的操作
    $(".itemList").html("");
    //3.1 获取请求参数
    //3.1.1 获取查询的关键词
    var keywords = $("#inputSeach").val();
    //3.1.2 获取搜索工具: 起止时间
    var startDate = $("[name=startDate]").val();
    var endDate = $("[name=endDate]").val();
    //3.1.3 获取搜索工具: 编辑
    var editor = $("[name=editor]").val();
    //3.1.3 获取搜索工具: 来源
    var source = $("[name=source]").val();

    //3.2 判断,如果没有任何参数, 直接让到首页 index.html
    /*if (keywords == null || keywords.trim() == "") {
        //跳转首页
        window.location.href = "/index.html";
    }*/
    //3.3 执行异步请求
    var params = {
        "keywords": keywords,
        "startDate": startDate,
        "endDate": endDate,
        "editor": editor,
        "source": source,
        "pageBean.page": page,
        "pageBean.pageSize": pageSize
    }
    $.post("/ps.action", params, function (data) { // 返回 pageBean对象

        if (data == null || data == "") {
            window.location.href = "/index.html";
            return;
        }

        // data 就是返回的json数据 : 集合 数组
        $(data.newsList).each(function () {

            //this: 当前遍历的对象  news对象
            /* <div class="item">
                     <div class="title"><a href="#">北京传智播客教育科技股份有限公司</a></div>
                 <div class="contentInfo_src">
                     <a href="#"><img src="./img/item.jpeg" alt="" class="imgSrc" width="121px" height="75px"></a>
                     <div class="infoBox">
                     <p class="describe">
                     大数据学习已然成为时代所趋，相较于目前市面上的书籍及学习视频，大数据培训更适用于对大数据感兴趣的人群，通过培训老师丰富的大数据实战经验分享， 能在大数据初期学习中，少走很多弯路，后期的项目实战，结合企业及时下大数据热门应用，可快速接轨大数据发展方向。
             </p>
                 <p><a class="showurl" href="www.itcast.cn">www.itcast.cn 2018-08</a> <span class="lab">隔壁老王 - 网易新闻</span></p>
                 </div>
                 </div>
                 </div>*/
            //一个news对象, 就是一个新闻,一个新闻其实就是div标签
            var docurl = this.docurl;
            docurl = docurl.substring(0, 20) + "...";
            var divStr = "<div class=\"item\">\n" +
                "                    <div class=\"title\"><a href=\"" + this.docurl + "\">" + this.title + "</a></div>\n" +
                "                <div class=\"contentInfo_src\">\n" +
                "                    <a href=\"#\"><img src=\"./img/item.jpeg\" alt=\"\" class=\"imgSrc\" width=\"121px\" height=\"75px\"></a>\n" +
                "                    <div class=\"infoBox\">\n" +
                "                    <p class=\"describe\">\n" +
                "                   " + this.content + " " +
                "            </p>\n" +
                "                <p><a class=\"showurl\" href=\"" + this.docurl + "\">" + docurl + "&nbsp;&nbsp;&nbsp;" + this.time + "</a> <span class=\"lab\">" + this.editor + " - " + this.source + "</span></p>\n" +
                "                </div>\n" +
                "                </div>\n" +
                "                </div>"

            $(".itemList").append(divStr);

        })

        /*<ul>
            <li><a href="#">< 上一页</a></li>
            <li>1</li>
            <li>2</li>
            <li class="on">3</li>
            <li>4</li>
            <li>5</li>
            <li>6</li>
            <li>7</li>
            <li>下一页 ></li>
        </ul>*/

        //分页条
        var pageStr = "<ul>";
        // 1.1 上一页 :
        //<li><a href="#">< 上一页</a></li>
        if (data.page > 1) {
            //如何禁用掉某一个元素的属性呢? js:javascript:void(0)
            pageStr += "<li><a href='javascript:void(0)' onclick='ajaxQuery(" + (data.page - 1) + ",15)'>< 上一页</a></li>";
        }

        //1.2 分页码数: 展示七个条码     前三 后三             1 2 3 4 5 6 7 8 9 10
        //如果总页数就7个:展示全部页面
        if (data.pageNumber <= 7) {
            //直接展示所有的页数
            for (var i = 1; i <= data.pageNumber; i++) {
                if (data.page == i) {
                    pageStr += "<li class='on'>" + i + "</li>";
                } else {
                    pageStr += "<li><a href='javascript:void(0)' onclick='ajaxQuery(" + i + ",15)' >" + i + "</a></li>"
                }

            }

        } else {
            //总页数大于7

            // 如果用户: 当前页 1~4  展示1~7
            if(data.page <=4){
                for(var i = 1 ; i<=7 ; i++){
                    if (data.page == i) {
                        pageStr += "<li class='on'>" + i + "</li>";
                    } else {
                        pageStr += "<li><a href='javascript:void(0)' onclick='ajaxQuery(" + i + ",15)' >" + i + "</a></li>"
                    }
                }
            }

            //如果用户: 当前页 大于4,展示: 当前页-3 ~ 当前页+3
            // 1 2 3 4 5 6 7 8 9 10
            if(data.page>4 && data.page<= data.pageNumber-3){

                for(var i =data.page-3 ; i<= data.page+3 ; i++ ){

                    if (data.page == i) {
                        pageStr += "<li class='on'>" + i + "</li>";
                    } else {
                        pageStr += "<li><a href='javascript:void(0)' onclick='ajaxQuery(" + i + ",15)' >" + i + "</a></li>"
                    }

                }
            }

            //如果当前页+3大于等于总页数: 展示 总页数-6
            if(data.page+3 >=data.pageNumber){
                for(var i = data.pageNumber-6 ; i<= data.pageNumber ; i++){
                    if (data.page == i) {
                        pageStr += "<li class='on'>" + i + "</li>";
                    } else {
                        pageStr += "<li><a href='javascript:void(0)' onclick='ajaxQuery(" + i + ",15)' >" + i + "</a></li>"
                    }
                }
            }

        }


        //1.3 下一页:
        if (data.page < data.pageNumber) {
            pageStr += "<li><a href='javascript:void(0)' onclick='ajaxQuery(" + (data.page + 1) + ",15)'>下一页 ></a></li>";
        }

        pageStr += "</ul>";

        $(".pageList").html(pageStr);

    }, "json")

}


function ajaxTopQuery(){
    //发送异步请求, 获取热搜词

    $.get("/top.action",function(data){

        var topList = $(data);
        if(topList.length == 0 ){
            //暂时不做任何的处理
            return;
        }

        var topDivStr = "";
        topList.each(function () {
            //this : map
            topDivStr += "<div class='item' ><span style='width:100%;' onclick='topkey()'>"+this.topKeyWords+"</span><span style='float: right; color: red'>"+this.score+"</span></div>";

        });
        $(".recommend").html(topDivStr);


    },"json")
}

function topkey() {
    // 获取到了关键词
    var val = $(this).html();

    //如何执行查询 : 重新执行页面
    window.location.href = "/list.html?keywords="+val;
}

