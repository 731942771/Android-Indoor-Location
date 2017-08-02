2016-12-13 19:02 发布于 h t t p : / / git.oschina.net/vigiles/Android-Indoor-Location-
<br/>
<br/>

基于安卓的室内wifi定位
=============

本项目没有想象中的高端。
思路就是在一个已确定的地图上指定作为参照的wifi热点位置，然后根据信号强弱判断手机所在位置。<br/>

##业务流程
如上所述<br/>
![github](/pic/wifi0.png)<br/>

##呈现界面
在此没有复杂设计，以说明设计思路为目的<br/>
![github](/pic/device-1.png)

##地图
地图事先绘制好，然后执行导入。<br/>
![github](/pic/device-2.png)

##扫描wifi
这个扫描功能当然是不断执行的，本项目将之放入了service。首次扫描后进入选择界面。<br/>
![github](/pic/device-3.png)

##参照物放置
将刚刚选择的wifi信号定位。<br/>
![github](/pic/device-4.png)

##最终效果
当然这个效果现在还很粗糙。初衷是根据参照物wifi的信号强弱计算，将箭头的位置实时更新。<br/>
![github](/pic/device-5.png)

<hr/>
现在还未完成的就是，根据所选的全部wifi的信号强度，设计一个算法，比如画圆圈取交集啥的，确定手机（箭头）的位置。
有空再完善。
搞得有点仓促，代码质量不高。
欢迎评论、讨论、指正。
<a href="http://www.cuiweiyou.com" target="_blank">cuiweiyou.com</a>
<br/>