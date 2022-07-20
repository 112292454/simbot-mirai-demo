package love.simbot.example.listener;


import DownloadTools.DownLoad;
import DownloadTools.QRCode;
import catcode.CatCodeUtil;
import catcode.Neko;
import love.forte.common.ioc.annotation.Beans;
import love.forte.common.ioc.annotation.Depend;
import love.forte.simbot.annotation.Filter;
import love.forte.simbot.annotation.OnGroup;
import love.forte.simbot.annotation.OnGroupMsgRecall;
import love.forte.simbot.api.message.MessageContent;
import love.forte.simbot.api.message.MessageContentBuilderFactory;
import love.forte.simbot.api.message.containers.AccountInfo;
import love.forte.simbot.api.message.containers.GroupInfo;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.message.events.GroupMsgRecall;
import love.forte.simbot.api.sender.Sender;
import love.forte.simbot.api.sender.Setter;
import love.forte.simbot.filter.MatchType;
import love.simbot.example.MyProduce;
import love.simbot.example.picFolderInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 群消息监听的示例类。
 * 所有需要被管理的类都需要标注 {@link Beans} 注解。
 * @author ForteScarlet
 */
@Beans
public class MyGroupListen {
    static Pattern p1= Pattern.compile("text\":.\"[^\"]+");//text":4"xxxxxx
    static Pattern number =Pattern.compile("\\b\\d+");//1324364 etc.
    static Pattern p3=Pattern.compile("name:\\w+");//name:xxxxxx
    static Pattern Dice=Pattern.compile("\\d+(d|D)\\d+");//aDb;adb
    static Pattern validID= Pattern.compile("([一-龟|\\w| ]+([- ])+)?[\\d|.| ]{2,}-*([一-龟|\\w| ]+([- ])+)*[一-龟|\\w| ]+");//buptIDCheck
    static HashSet<String> checkedID=new HashSet<>();
    static QRCode q=new QRCode().setToken();
    static 	CatCodeUtil util = CatCodeUtil.INSTANCE;

    private static final String BUPTHELP="看群公告群文件群相册群精华消息\n\n"
            +"学校官网(需有学号后使用)： webvpn.bupt.edu.cn  常用信息门户、本科教务管理系统\n"
            +"进群记得修改群名片 例如【20-河南-计算机-汪哈羊】"
            +util.toCat("image", true, "file=" +"D:\\botPic\\bupt\\tips.jpg")
            +"功能：学校地图，学校网址，宿舍规格";

    /** log */
    private static final Logger LOG = LoggerFactory.getLogger(MyGroupListen.class);
    @Depend
    private MessageContentBuilderFactory builderFactory;

    @Autowired
    MyProduce myProduce;

    /**
     *
     *
     * @Author  Guo
     * @CreateTime   2022-05-20 00:08
     * @Return void
     * @Discription 二维码识别
     * @param groupMsg
     * @param sender
     */
    @OnGroup
    @Filter(value = "/qr",matchType = MatchType.STARTS_WITH)
    public void QRCode(GroupMsg groupMsg,Sender sender) {
        String msg=groupMsg.getMsg();
        boolean auth=myProduce.getAuth(groupMsg.getGroupInfo().getGroupCode(),"basic");
        System.out.println(groupMsg.getAccountInfo().getAccountCode()+"发送了"+msg+"要求二维码识别");
        MessageContent msgContent = groupMsg.getMsgContent();
        //获取所有图片链接并得到其中的二维码识别结果
        HashSet<String> urls=new HashSet<>();
        List<Neko> imageCats = msgContent.getCats("image");
        System.out.println("img counts: " + imageCats.size());
        for (Neko image : imageCats) {
            String s=image.get("url");
            if(myProduce.VisQR.containsKey(s)){
                urls.add(myProduce.VisQR.get(s));
                continue;
            }
            String res=q.UrlQRCode(s);
            Matcher m1=p1.matcher(res);
            while ((m1.find())) {
                res=m1.group().substring(8);
                urls.add(res);
                myProduce.VisQR.put(s,res);
            }
        }
        StringBuilder sb=new StringBuilder();
        //单次发送链接最大长约950字符，则手机端可以识别标蓝
        for (String url : urls) {
            if(DownLoad.isConnect(url)) {
                if(sb.length()+url.length()<950) {
                    sb.append("有效："+url+"\n");
                }else{
                    myProduce.sendMsg(groupMsg,sender,sb.toString(),auth);
                    sb=new StringBuilder(url+"\n");
                }
            }else{
                if(sb.length()+url.length()<950) {
                    sb.append(url+"\n");
                }else{
                    myProduce.sendMsg(groupMsg,sender,sb.toString(),auth);
                    sb=new StringBuilder(url+"\n");
                }
            }
        }
        /*sender.sendGroupMsg(groupMsg.getGroupInfo(),"1");
        sender.sendGroupMsg(groupMsg.getGroupInfo(),sb.toString());
        sender.sendPrivateMsg(groupMsg.getAccountInfo().getAccountCode(),groupMsg.getGroupInfo().getGroupCode(),sb.toString());*/
        myProduce.sendMsg(groupMsg,sender,sb.toString(),auth);
    }

    /**
     *
     *
     * @Author  Guo
     * @CreateTime   2022-05-20 00:10
     * @Return void
     * @Discription 随机网络涩图
     * @param groupMsg
     * @param sender
     */
    @OnGroup
    @Filter(value = "/ir",matchType = MatchType.STARTS_WITH)
    public void getRandomNetPic(GroupMsg groupMsg, Sender sender){
        // 获取消息正文。
        boolean auth=myProduce.getAuth(groupMsg.getGroupInfo().getGroupCode(),"pic");
        String text=groupMsg.getText();
        CatCodeUtil util = CatCodeUtil.INSTANCE;
        int num = 1;
        Matcher m= number.matcher(text);
        if(m.find()) {
            num = Integer.parseInt(m.group().trim());
        }
        System.out.println(groupMsg.getAccountInfo().getAccountCode()+"发送了"+groupMsg.getMsg()+"要求"+num+"张网络图片");
        StringBuilder sb=new StringBuilder();
        for (int i = 0; i < num; i++) {
            int id= myProduce.idleId.pollLast();
            String path=myProduce.netPicPath +id+".jpg";
            Random r=new Random(25);
            int k=r.nextInt(100);
            if(k>=20) {
                myProduce.down( "https://iw233.cn/api/Random.php",path);
            } else if(k>=10) {
                myProduce.down("https://api.ghser.com/random/pc.php",path);
            }else {
                myProduce.down( "https://api.ixiaowai.cn/api/api.php",path);
            }
            String image = util.toCat("image", true,"file="+path);
            if(new File(path).exists()){
                sb.append("netPID"+id +":"+ image);
                myProduce.evaluation.put(id,0);
            }
            /*imgCount++;
            if(imgCount%5==0) {
                myProduce.finish();
            }*/
        }
        myProduce.sendMsg(groupMsg,sender,sb.toString(),auth);
    }

    /**
     *
     *
     * @Author  Guo
     * @CreateTime   2022-05-20 00:10
     * @Return void
     * @Discription 本地涩图
     * @param groupMsg
     * @param sender
     */
    @OnGroup
    @Filter(value = "/lr",matchType = MatchType.STARTS_WITH)
    public void getRandomLocalPic(GroupMsg groupMsg, Sender sender) {
        // 获取消息正文。
        String text = groupMsg.getText().trim();
        boolean auth=myProduce.getAuth(groupMsg.getGroupInfo().getGroupCode(),"pic");
        int num = 1;
        Matcher m= number.matcher(text);
        if(m.find()) {
            num = Integer.parseInt(m.group().trim());
        }
        num=Math.min(num,5);

        LocalPicMethod1(groupMsg, sender, text, auth, num);
    }



    @OnGroup
    @Filter(value = "来点",matchType = MatchType.STARTS_WITH)
    public void RandomLocalPicReuse(GroupMsg groupMsg, Sender sender) {
        // 获取消息正文。
        String text = groupMsg.getText().trim();
        if(!text.endsWith("涩图")&&!text.endsWith("色图")) {
            return;
        }
        text=text.replace("来点"," ").replace("涩图"," ").replace("色图"," ");
        boolean auth=myProduce.getAuth(groupMsg.getGroupInfo().getGroupCode(),"pic");
        int num = 3;

        LocalPicMethod1(groupMsg, sender, text, auth, num);
    }

    private void LocalPicMethod1(GroupMsg groupMsg, Sender sender, String text, boolean auth, int num) {
        String flag = "localPic";
        if (text.contains("ff")) {
            flag = "ff14";
        }else if(text.contains("福利姬")){
            flag = "福利姬";
        }
        else if(text.contains("伪娘")){
            flag = "伪娘";
        }
        if (text.contains("h")) {
            flag += "h";
            auth= myProduce.getAuth(groupMsg.getGroupInfo().getGroupCode(),"r18");
        }
        picFolderInfo folderInfo=myProduce.getFolderPath(flag);
        System.out.println(groupMsg.getAccountInfo().getAccountCode() + "发送了" + groupMsg.getMsg() + "要求" + num + "张"+flag+"图片");
        StringBuilder sb = new StringBuilder();
        CatCodeUtil util = CatCodeUtil.INSTANCE;
        int size = myProduce.localSize, id;
        Random r = new Random();
        for (int i = 0; i < num; i++) {
            id = r.nextInt(folderInfo.size());
            String image = util.toCat("image", true, "file=" + folderInfo.get(id));
            String name=folderInfo.getName(id);
            System.out.println("图片" + name);
            if(flag.contains("local")){
                sb.append("name:").append(name);
            }
            sb.append(image);
            myProduce.setSendPicPath(name,folderInfo.get(id));
            myProduce.sendMsg(groupMsg, sender, sb.toString(),auth);
            sb=new StringBuilder();
            if(!auth){
                return;
            }
        }
        myProduce.sendMsg(groupMsg, sender, sb.toString(),auth);
    }
    /**
     *
     *
     * @Author  Guo
     * @CreateTime   2022-07-20 00:49
     * @Return void
     * @Discription 贴贴
     * @param groupMsg
     * @param sender
     */
    @OnGroup
    @Filter(value = "贴贴",matchType = MatchType.ENDS_WITH)
    public void tietie(GroupMsg groupMsg, Sender sender){
        if(groupMsg.getMsgContent().toString().contains("3425460643")){
            picFolderInfo folderInfo=myProduce.getFolderPath("贴贴");
            int index=new Random().nextInt(folderInfo.size());
            myProduce.sendMsg(groupMsg, sender, util.toCat("image", true, "file=" +folderInfo.get(index)), true);
        }
    }
    /**
     *
     *
     * @Author  Guo
     * @CreateTime   2022-05-20 00:10
     * @Return void
     * @Discription 骰子
     * @param groupMsg
     * @param sender
     */
    @OnGroup
    @Filter(value = "/r",matchType = MatchType.STARTS_WITH)
    public void random(GroupMsg groupMsg, Sender sender){
        CatCodeUtil util = CatCodeUtil.INSTANCE;

        String text=groupMsg.getText().toLowerCase(Locale.ROOT);
        boolean auth=myProduce.getAuth(groupMsg.getGroupInfo().getGroupCode(),"basic");
        Matcher dice=Dice.matcher(text),num=number.matcher(text);
        int range=100,res=0,max=50;
        Random random=new Random();
        StringBuilder builder=new StringBuilder();
        if(text.startsWith("/ra")){
            if(num.find()){
                max=Integer.parseInt(num.group());
            }
            if(max>100||max<0){
                myProduce.sendMsg(groupMsg,sender,"错误的成功率",auth);
                return;
            }
            res=random.nextInt(range)+1;
            text=text.substring(3).trim();
            String name="default";
            for (String d : text.split(" ")) {
                try {
                    Integer.parseInt(d.trim());
                }catch (NumberFormatException n){
                    name=d;
                    break;
                }
            }
            builder.append(groupMsg.getAccountInfo().getAccountRemark());
            builder.append("进行").append(name).append("检定：D100=");
            builder.append(res).append("/").append(max).append((res<=5||res>=95)?"  大":"  ").append(res>max?"失败":"成功");
            myProduce.sendMsg(groupMsg,sender,builder.toString(),auth);
        }else if(dice.find()){
            String[] n=dice.group().split("d");
            int times=Integer.parseInt(n[0]);
            range=Integer.parseInt(n[1]);
            for (int i = 0; i < times; i++) {
                res+=random.nextInt(range)+1;
            }
            myProduce.sendMsg(groupMsg,sender,times+"d"+range+"="+res,auth);
        } else if(num.find()){
            range=Integer.parseInt(num.group());
            myProduce.sendMsg(groupMsg,sender,"1d"+range+"="+(random.nextInt(range)+1),auth);
        }else{
            myProduce.sendMsg(groupMsg,sender,"1d"+range+"="+(random.nextInt(range)+1),auth);
        }
        System.out.println(groupMsg.getAccountInfo().getAccountCode()+"要求一个随机数");


    }

    /**
     *
     *
     * @Author  Guo
     * @CreateTime   2022-05-20 00:10
     * @Return void
     * @Discription 识图——低配版
     * @param groupMsg
     * @param sender
     */
    @OnGroup
    @Filter(value = "/sau",matchType = MatchType.STARTS_WITH)
    public void imageSauce(GroupMsg groupMsg, Sender sender){
        String text=groupMsg.getText().toLowerCase(Locale.ROOT),msg=groupMsg.getMsg();
        boolean auth=myProduce.getAuth(groupMsg.getGroupInfo().getGroupCode(),"basic");
        List<Neko> imageCats = groupMsg.getMsgContent().getCats("image");
        System.out.println("img counts: " + imageCats.size());
        for (Neko image : imageCats) {
            String url=image.get("url");
            url=url.replaceAll("/","%2F");
            url=url.replaceAll(":","%3A");
            url=url.replaceAll("\\?","%3F");
            url="https://saucenao.com/search.php?db=999&output_type=0&testmode=1&numres=3&hide=0&url="+url;
            myProduce.sendMsg(groupMsg,sender,url,auth);
            System.out.println("Img url: " + image.get("url"));
        }

        System.out.println(groupMsg.getAccountInfo().getAccountCode()+"要求图片识别——低配版");


    }

    /**
     *
     *
     * @Author  Guo
     * @CreateTime   2022-05-20 00:11
     * @Return void
     * @Discription 帮助
     * @param groupMsg
     * @param sender
     */
    @OnGroup
    @Filter(value = "/help",matchType = MatchType.CONTAINS)
    public void helpMsg(GroupMsg groupMsg, Sender sender){
        boolean auth=myProduce.getAuth(groupMsg.getGroupInfo().getGroupCode(),"basic");
        System.out.println(groupMsg.getAccountInfo().getAccountCode()+"发送了"+groupMsg.getMsg()+"希望获得帮助");
        StringBuilder builder=new StringBuilder();
        builder.append("当前功能（[xx]为可选项）:\n");
        builder.append(" 1、“/ir [数量]“得到随机二刺猿图\n");
        builder.append(" 2、”/qr 图片“（可一次多张）识别其中可能存在的二维码链接\n");
        builder.append(" 3、“/lr [类型] [数量]“得到本地图片\n");
        if(myProduce.getAuth(groupMsg.getGroupInfo().getGroupCode(),"pic")){
            builder.append("    类型有：ff14、ff14h、（空白）、h、福利姬(图最多）\n");
        }
        builder.append(" 4、”/r [x]“,默认x=100;“/ra [x(成功率)] [名称]”\n");
        //builder.append(" 5、“/sau 图片” 为识图（究级低配版）\n");
        //builder.append(" 消息最后包含“private”则只发送私聊");
        myProduce.sendMsg(groupMsg,sender,builder.toString(),auth);
    }

    /**
     *
     *
     * @Author  Guo
     * @CreateTime    2022-07-20 00:11
     * @Return void
     * @Discription bupt相关资料
     * @param groupMsg
     * @param sender
     */

    @OnGroup
    public void OtherCheck(GroupMsg groupMsg, Sender sender){
        String txt=groupMsg.getText().toLowerCase(),groupCode=groupMsg.getGroupInfo().getGroupCode(),userCode=groupMsg.getAccountInfo().getAccountCode();
        String res="";
        if("475954521".equals(groupCode)){
            if(txt.equals("bupt")){
                res=BUPTHELP;
            }else if(txt.contains("宿舍规格")){
                res=util.toCat("image", true, "file=" +"D:\\botPic\\bupt\\宿舍.jpg");
            }else if(txt.equals("学校地图")){
                res=util.toCat("image", true, "file=" +"D:\\botPic\\bupt\\校园地形.jpg");
            }else if(txt.equals("本部地图")){
                res=util.toCat("image", true, "file=" +"D:\\botPic\\bupt\\本部地图.jpg");
            }else if(txt.contains("wifi")&&txt.contains("怎么")){
                res=util.toCat("image", true, "file=" +"D:\\botPic\\bupt\\wifi.jpg");
            }else if(txt.equals("学校网址")){
                res="选课使用教务系统，如果无法直接打开可以通过VPN登陆。\n" +
                        "WebVpn系统（在校外访问内?必备）： https://webvpn.bupt.edu.cn/login以及https://libcon.bupt.edu.cn/  \n" +
                        "北邮人bt(只有ipv6才能访问)： http://byr.pt \n" +
                        "信息门户： http://my.bupt.edu.cn/ \n" +
                        "教务系统： https://jwgl.bupt.edu.cn/jsxsd/ \n" +
                        "北邮人论坛： https://bbs.byr.cn/\n" +
                        "图书馆预约： http://order.bupt.edu.cn/\n" +
                        "爱课堂： https://iclass.bupt.edu.cn/  \n" +
                        "22招生宣传： https://mp.weixin.qq.com/s/2hcgfdnDalK9MpHZzLnPCA";
            }
        }
        if("852209848".equals(groupCode)){
            if("939570061".equals(userCode)){
                if(txt.contains("啵")||txt.contains("播")) res="啵";
            }
        }
        myProduce.sendMsg(groupMsg,sender,res,true);
    }

    @OnGroup
    @Filter(value = "宿舍规格",matchType = MatchType.CONTAINS)
    public void sushe(GroupMsg groupMsg, Sender sender){
        System.out.println(groupMsg.getAccountInfo().getAccountCode()+"发送了"+groupMsg.getMsg()+"希望获得bupt帮助");
        myProduce.sendMsg(groupMsg,sender,util.toCat("image", true, "file=" +"D:\\botPic\\bupt\\宿舍.jpg"),true);
    }
    @OnGroup
    @Filter(value = "学校地图",matchType = MatchType.EQUALS)
    public void map(GroupMsg groupMsg, Sender sender){
        System.out.println(groupMsg.getAccountInfo().getAccountCode()+"发送了"+groupMsg.getMsg()+"希望获得bupt帮助");
        myProduce.sendMsg(groupMsg,sender,util.toCat("image", true, "file=" +"D:\\botPic\\bupt\\校园地形.jpg"),true);
    }
    @OnGroup
    @Filter(value = "本部地图",matchType = MatchType.EQUALS)
    public void originMap(GroupMsg groupMsg, Sender sender){
        System.out.println(groupMsg.getAccountInfo().getAccountCode()+"发送了"+groupMsg.getMsg()+"希望获得bupt帮助");
        myProduce.sendMsg(groupMsg,sender,util.toCat("image", true, "file=" +"D:\\botPic\\bupt\\本部地图.jpg"),true);
    }
    @OnGroup
    @Filter(value = "学校网址",matchType = MatchType.EQUALS)
    public void wangzhi(GroupMsg groupMsg, Sender sender){
        System.out.println(groupMsg.getAccountInfo().getAccountCode()+"发送了"+groupMsg.getMsg()+"希望获得bupt帮助");
        myProduce.sendMsg(groupMsg,sender,"选课使用教务系统，如果无法直接打开可以通过VPN登陆。 \nWebVpn系统（在校外访问内?必备）： https://webvpn.bupt.edu.cn/login以及https://libcon.bupt.edu.cn/  \n北邮人bt(只有ipv6才能访问)： http://byr.pt \n信息门户： http://my.bupt.edu.cn/ \n教务系统： https://jwgl.bupt.edu.cn/jsxsd/ \n" +
                "北邮人论坛： https://bbs.byr.cn/\n图书馆预约： http://order.bupt.edu.cn/\n爱课堂： https://iclass.bupt.edu.cn/  \n22招生宣传： https://mp.weixin.qq.com/s/2hcgfdnDalK9MpHZzLnPCA",true);
    }
    @OnGroup
    @Filter(value = "海南学院",matchType = MatchType.EQUALS)
    public void hainan(GroupMsg groupMsg, Sender sender){
        System.out.println(groupMsg.getAccountInfo().getAccountCode()+"发送了"+groupMsg.getMsg()+"希望获得bupt帮助");
        myProduce.sendMsg(groupMsg,sender,util.toCat("image", true, "file=" +"D:\\botPic\\bupt\\海南学院.jpg"),true);
    }

    @OnGroup
    public void IDCheck(GroupMsg groupMsg,Sender sender) {
        /*if(!"745769821".equals(groupMsg.getGroupInfo().getGroupCode())){
            return;
        }*/
        if(new Random().nextInt(5)!=1) {
            return;
        }
        if(!"475954521".equals(groupMsg.getGroupInfo().getGroupCode())){
            return;
        }
        checkedID.add("770816116");
        checkedID.add("3214447142");

        String id=groupMsg.getAccountInfo().getAccountRemark(),code=groupMsg.getAccountInfo().getAccountCode();
        String send=util.toCat("at",true,"code="+code);
        if(id == null){
            id="!";
        }
        {
            id=id.replace((char) 8211, (char) 45)
                    .replace((char) 65293, (char) 45)
                .replace((char) 8722, (char) 45)
                .replace((char) 8212, (char) 45)
                .replace('+', (char) 45)
                .replace('/', (char) 45)
                .replace('(', (char) 45)
                .replace(')', (char) 45)
                .replace('（', (char) 45)
                .replace('）', (char) 45)
                .replace('、', (char) 45);
        }
        System.out.println("检查buptid："+id);
        send+="记得修改群名片 例如【20-所在省-计算机-汪哈羊】";
        Matcher m=validID.matcher(id);
        boolean match=validID.matcher(id).matches(),find=m.find()||id.length()>11;
        if(match) {
            System.out.println("格式正确");
            return;
        }else if(find){
            String s;
            try {
                s=m.group();
            }catch (IllegalStateException e){
                e.printStackTrace();
                s="!";
            }
            if(id.startsWith(s)||id.endsWith(s)){
                System.out.println("不符，但以合理id开头/结尾，跳过");
                return;
            }
            int t=new Random().nextInt(5);
            System.out.println("存在格式不符id,判断："+t+(t==3));
            if(t==3&&!checkedID.contains(code)){
                myProduce.sendMsg(groupMsg,sender,send,true);
            }
            checkedID.add(code);
        }else if(!checkedID.contains(code)) {
            System.out.println("存在非法id");
            checkedID.add(code);
            /*if(new Random().nextInt(3)==1){
                checkedID.remove(code);
            }*/
            myProduce.sendMsg(groupMsg,sender,send,true);
        }
    }

    @OnGroup
    @Filter(value = "/finish",matchType = MatchType.CONTAINS)
    public void closeRobot(GroupMsg groupMsg, Sender sender){
        if(!"1154459434".equals(groupMsg.getAccountInfo().getAccountCode())) {
            System.out.println("invalid user finished");
            return;
        }
        myProduce.finish();
        System.out.println("save success");
    }


    /*@OnGroup
    @Filter(value = "down",matchType = MatchType.CONTAINS)
    public void VoteDown(GroupMsg groupMsg){
        // 获取消息正文。
        System.out.println("进入downvote");
        String text=groupMsg.getText().toLowerCase(Locale.ROOT).trim();
        String s=groupMsg.getMsg();
        String name=p3.matcher(s).group();
        StringBuilder res=new StringBuilder();
        GroupAccountInfo info=groupMsg.getAccountInfo();
        int idindex=s.indexOf("netPID")+6;
        if(idindex==7||!s.startsWith("[CAT:quote")) {
            return;
        }
        System.out.println(s);
        while (Character.isDigit(s.charAt(idindex))){
            res.append(s.charAt(idindex++));
        }
        idindex=Integer.parseInt(res.toString());
        if(text.contains("down")) {
            myProduce.evaluation.put(idindex,myProduce.evaluation.get(idindex)-1);
            System.out.println(info.getAccountNickname()+"("+info.getAccountCode()+")对id为"+idindex+"的图片做出了-1的评价");
        }
        System.out.println("图片"+idindex + "当前评价为" + myProduce.evaluation.get(idindex));
    }
    @OnGroup
    @Filter(value = "up",matchType = MatchType.CONTAINS)
    public void VoteUp(GroupMsg groupMsg){
        // 获取消息正文。
        System.out.println("进入downvote");
        String s=groupMsg.getMsg(),vote=groupMsg.getText().toLowerCase();
        StringBuilder res=new StringBuilder();
        GroupAccountInfo info=groupMsg.getAccountInfo();
        int idindex=s.indexOf("netPID")+6;
        if(idindex==7||!s.startsWith("[CAT:quote")) {
            return;
        }
        System.out.println(s);
        while (Character.isDigit(s.charAt(idindex))){
            res.append(s.charAt(idindex++));
        }
        idindex=Integer.parseInt(res.toString());
        if(vote.indexOf("up")!=-1) {
            myProduce.evaluation.put(idindex,myProduce.evaluation.get(idindex)+1);
            System.out.println(info.getAccountNickname()+"("+info.getAccountCode()+")对id为"+idindex+"的图片做出了+1的评价");

        } else if(vote.indexOf("down")!=-1) {
            myProduce.evaluation.put(idindex,myProduce.evaluation.get(idindex)-1);
            System.out.println(info.getAccountNickname()+"("+info.getAccountCode()+")对id为"+idindex+"的图片做出了-1的评价");
        }
        System.out.println("图片"+idindex + "当前评价为" + myProduce.evaluation.get(idindex));
    }
    */
    @OnGroup
    @Filter(value = "del",matchType = MatchType.CONTAINS)
    public void delete(GroupMsg groupMsg, Setter setter){
        // 获取消息正文。
        System.out.println("进入delete");
        String s=groupMsg.getMsg(),name;
        Matcher m=p3.matcher(s);
        if(m.find()){
            name=m.group().substring(5);
            //setter.setMsgRecall(groupMsg.getFlag());
        }else {
            return;
        }
        System.out.println(myProduce.deleteByName(name));
    }
    /*@OnGroup
    public void onGroupMsg(GroupMsg groupMsg,Sender sender) {

        // 打印此次消息中的 纯文本消息内容。
        // 纯文本消息中，不会包含任何特殊消息（例如图片、表情等）。
        System.err.println("text:");
        System.out.println(groupMsg.getText());

        // 打印此次消息中的 消息内容。
        // 消息内容会包含所有的消息内容，也包括特殊消息。特殊消息使用CAT码进行表示。
        // 需要注意的是，绝大多数情况下，getMsg() 的效率低于甚至远低于 getText()
        System.err.println("Msg:");
        String str=groupMsg.getMsg();
        System.out.println(str);

        // 获取此次消息中的 消息主体。
        // messageContent代表消息主体，其中通过可以获得 msg, 以及特殊消息列表。
        // 特殊消息列表为 List<Neko>, 其中，Neko是CAT码的封装类型。

        MessageContent msgContent = groupMsg.getMsgContent();
        // 打印消息主体
        System.err.println("messageContent:");
        System.out.println(msgContent);
        if(msgContent.toString().contains("flash")){
            String s=str.substring(str.indexOf("url="));
            System.err.println("有闪照：\nMsg:");
            System.out.println(s);
            sender.sendPrivateMsg("3425460643",s);
            List<Neko> imageCats = groupMsg.getMsgContent().getCats("image");
            System.out.println("img counts: " + imageCats.size());
            AccountInfo accountInfo = groupMsg.getAccountInfo();
            GroupInfo groupInfo = groupMsg.getGroupInfo();
            StringBuilder name=new StringBuilder();
            name.append(accountInfo.getAccountCode()).append("——")
                    .append(accountInfo.getAccountNickname()).append("____")
                    .append(groupInfo.getGroupName());
            for (Neko image : imageCats) {
                DownLoad.down(image.get("url"),"D:\\botPic\\backupPics\\",name.toString()+(System.currentTimeMillis()%1000000));
                System.out.println("Img url: " + image.get("url"));
            }
            System.out.println(name + "\n\n");
        }
        // 打印消息主体中的所有图片的链接（如果有的话）
        List<Neko> imageCats = msgContent.getCats("image");
        System.out.println("img counts: " + imageCats.size());
        for (Neko image : imageCats) {
            System.out.println("Img url: " + image.get("url"));
        }

        // 获取发消息的人。
        GroupAccountInfo accountInfo = groupMsg.getAccountInfo();
        // 打印发消息者的账号与昵称。
        System.out.println(accountInfo.getAccountCode());
        System.out.println(accountInfo.getAccountNickname());


        // 获取群信息
        GroupInfo groupInfo = groupMsg.getGroupInfo();
        // 打印群号与名称
        System.out.println(groupInfo.getGroupCode());
        System.out.println(groupInfo.getGroupName()+"\n\n");

    }*/
    @OnGroupMsgRecall
    public void recallMsg(GroupMsgRecall groupMsg, Sender sender){
        System.err.println("有撤回消息：\nMsg:");
        String str=groupMsg.getMsg();
        System.out.println(str);
        List<Neko> imageCats = groupMsg.getMsgContent().getCats("image");
        System.out.println("img counts: " + imageCats.size());
        AccountInfo accountInfo = groupMsg.getAccountInfo();
        GroupInfo groupInfo = groupMsg.getGroupInfo();
        StringBuilder name=new StringBuilder();
        name.append(accountInfo.getAccountCode()).append("——")
                .append(accountInfo.getAccountNickname()).append("____")
                .append(groupInfo.getGroupName());
        for (Neko image : imageCats) {
            DownLoad.down(image.get("url"),"D:\\botPic\\backupPics\\",name.toString()+(System.currentTimeMillis()%1000000));
            System.out.println("Img url: " + image.get("url"));
        }
        System.out.println(name + "\n\n");
    }
    /*@OnGroup
    @Filter(value = "/special")
    public void special(GroupMsg groupMsg, Sender sender){
        try {
            BufferedInputStream in ;//= new BufferedInputStream(new URL("https://doubi.ren/zuanbot/api.php?level=max").openStream());

            System.out.println("special");
            //sender.sendPrivateMsg("1154459434",new String(temp));
            for (int i = 0; i < 500; i++) {
                byte[] temp=new byte[2048];
                for (int j = 0; j < 500000000; j++) {
                    if(j>999999) {j++;}
                }
                in=new BufferedInputStream(new URL("https://doubi.ren/zuanbot/api.php?level=max").openStream());
                in.read(temp);
                sender.sendPrivateMsg("2822795227","829090488",new String(temp));

            }
            System.out.println("special");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/







}
