package love.simbot.example.listener;

import DownloadTools.DownLoad;
import DownloadTools.QRCode;
import catcode.CatCodeUtil;
import catcode.Neko;
import love.forte.common.ioc.annotation.Beans;
import love.forte.common.ioc.annotation.Depend;
import love.forte.simbot.annotation.Filter;
import love.forte.simbot.annotation.OnPrivate;
import love.forte.simbot.api.message.MessageContent;
import love.forte.simbot.api.message.MessageContentBuilderFactory;
import love.forte.simbot.api.message.events.PrivateMsg;
import love.forte.simbot.api.sender.Sender;
import love.forte.simbot.filter.MatchType;
import love.simbot.example.MyProduce;
import love.simbot.example.picFolderInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 私聊消息监听的示例类。
 * 所有需要被管理的类都需要标注 {@link Beans} 注解。
 * @author ForteScarlet
 */
@Beans
public class MyPrivateListen {
    static Pattern p1= Pattern.compile("text\":.\"[^\"]+");//text":4"xxxxxx
    static Pattern number =Pattern.compile("\\b\\d+");//1324364 etc.
    static Pattern p3=Pattern.compile("name:\\w+");//name:xxxxxx
    static Pattern Dice=Pattern.compile("\\d+(d|D)\\d+");//aDb;adb
    /**
     * 通过依赖注入获取一个 "消息正文构建器工厂"。
     *
     */
    @Depend
    private MessageContentBuilderFactory messageContentBuilderFactory;

    /**
     * 此监听函数监听一个私聊消息，并会复读这个消息，然后再发送一个表情。
     * 此方法上使用的是一个模板注解{@link OnPrivate}，
     * 其代表监听私聊。
     * 由于你监听的是私聊消息，因此参数中要有个 {@link PrivateMsg} 来接收这个消息实体。
     *
     * 其次，由于你要“复读”这句话，因此你需要发送消息，
     * 因此参数中你需要一个 "消息发送器" {@link Sender}。
     *
     * 当然，你也可以使用 {@link love.forte.simbot.api.sender.MsgSender}，
     * 然后 {@code msgSender.SENDER}.
     */
    @OnPrivate
    @Filter(value = "/ir",matchType = MatchType.STARTS_WITH)
    public void getRandomNetPic(PrivateMsg privateMsg, Sender sender){
        // 获取消息正文。
        System.out.println(privateMsg.getAccountInfo().getAccountCode()+"发送了"+privateMsg.getMsg()+"要求一张涩图");
        CatCodeUtil util = CatCodeUtil.INSTANCE;
        int id= MyProduce.idleId.pollLast();
        String path= MyProduce.netPicPath +id+".jpg";
        Random r=new Random(25);

        int k=r.nextInt(100);
        if(k>=50) {
            MyProduce.down( "https://api.ixiaowai.cn/api/api.php",path);
        } else {
            MyProduce.down("https://api.ghser.com/random/pc.php",path);
        }
        // 构建image, 第二个参数为true代表参数值需要进行转义
        String image = util.toCat("image", true,"file="+path);
        // 多个CAT码、CAT码与文本消息之间直接进行拼接
        sender.sendPrivateMsg(privateMsg,"localPID"+id +":"+ image);
        MyProduce.evaluation.put(id,0);
        /*
        // 向 privateMsg 的账号发送消息，消息为当前接收到的消息。
        //sender.sendPrivateMsg(privateMsg, msgContent);

        // 再发送一个表情ID为'9'的表情。
        // 方法1：使用消息构建器构建消息并发送
        // 在绝大多数情况下，使用消息构建器所构建的消息正文 'MessageContent'
        // 是用来发送消息最高效的选择。
        // 相对的，MessageContentBuilder所提供的构建方法是十分有限的。

        // 获取消息构建器
        MessageContentBuilder msgBuilder = messageContentBuilderFactory.getMessageContentBuilder();
        // 通过.text(...) 向builder中追加一句话。
        // 通过.face(ID) 向builder中追加一个表情。
        // 通过.build() 构建出最终消息。
        MessageContent msg = msgBuilder.text("表情：").face(9).build();

        // 直接通过这个msg发送。
        sender.sendPrivateMsg(privateMsg, msg);

        // 方法2：使用CAT码发送消息。
        // 使用CAT码构建一个需要解析的消息是最灵活的，
        // 但是相对的，它的效率并不是十分的可观，毕竟在这其中可能会涉及到很多的'解析'操作。

        // 获取CAT码工具类实例
        CatCodeUtil catCodeUtil = CatCodeUtil.getInstance();

        // 构建一个类型为 'face', 参数为 'id=9' 的CAT码。
        // 有很多方法。

        // 1. 通过 codeBuilder 构建CAT码
        // String cat1 = catCodeUtil.getStringCodeBuilder("face", false).key("id").value(9).build();

        // 2. 通过CatCodeUtil.toCat 构建CAT码
        // String cat2 = catCodeUtil.toCat("face", "id=9");

        // 3. 通过模板构建CAT码
        String cat3 = catCodeUtil.getStringTemplate().face(9);

        // 在cat码前增加一句 '表情' 并发送
        sender.sendPrivateMsg(privateMsg, "表情：" + cat3);
*/
    }

    @OnPrivate
    @Filter(value = "/qr",matchType = MatchType.STARTS_WITH)
    public void QRCode(PrivateMsg privatemsg, Sender sender) {
        String msg=privatemsg.getMsg();
        boolean onlyPrivate= msg.contains("private"),auth= MyProduce.getAuth(privatemsg.getAccountInfo().getAccountCode(),"basic",true);
        System.out.println(privatemsg.getAccountInfo().getAccountCode()+"发送了"+msg+"要求二维码识别");
        MessageContent msgContent = privatemsg.getMsgContent();
        QRCode q=new QRCode();
        //获取所有图片链接并得到其中的二维码识别结果
        HashSet<String> urls=new HashSet<>();
        List<Neko> imageCats = msgContent.getCats("image");
        System.out.println("img counts: " + imageCats.size());
        for (Neko image : imageCats) {
            String s=image.get("url");
            if(MyProduce.VisQR.containsKey(s)){
                urls.add(MyProduce.VisQR.get(s));
                continue;
            }
            String res=q.UrlQRCode(s,"24.652669a8ede8960f58e74153016bf9b8.2592000.1654897645.282335-25939155");
            Matcher m1=p1.matcher(res);
            while ((m1.find())) {
                res=m1.group().substring(8);
                urls.add(res);
                MyProduce.VisQR.put(s,res);
            }
        }
        StringBuilder sb=new StringBuilder();
        //单次发送链接最大长约950字符，则手机端可以识别标蓝
        for (String url : urls) {
            if(DownLoad.isConnect(url)) {
                if(sb.length()+url.length()<950) {
                    sb.append(url+"\n");
                }else{
                    MyProduce.sendMsg(privatemsg,sender,sb.toString(),auth);
                    sb=new StringBuilder(url+"\n");
                }
            }
        }
        MyProduce.sendMsg(privatemsg,sender,sb.toString(),auth);
    }

    @OnPrivate
    @Filter(value = "/lr",matchType = MatchType.STARTS_WITH)
    public void getRandomLocalPic(PrivateMsg privatemsg, Sender sender) {
        // 获取消息正文。
        String text = privatemsg.getText().trim(),code=privatemsg.getAccountInfo().getAccountCode();
        boolean onlyPrivate = text.contains("private"),auth= MyProduce.getAuth(code,"pic",true);
        int num = 1;
        Matcher m= number.matcher(text);
        if(m.find()) {
            num = Integer.parseInt(m.group().trim());
        }
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
            auth= MyProduce.getAuth(privatemsg.getAccountInfo().getAccountCode(),"r18");
        }
        picFolderInfo folderInfo= MyProduce.getFolderPath(flag);
        System.out.println(privatemsg.getAccountInfo().getAccountCode() + "发送了" + privatemsg.getMsg() + "要求" + num + "张"+flag+"图片");
        StringBuilder sb = new StringBuilder();
        CatCodeUtil util = CatCodeUtil.INSTANCE;
        int size = MyProduce.localSize, id;
        Random r = new Random();
        for (int i = 0; i < num; i++) {
            id = r.nextInt(folderInfo.size());
            String image = util.toCat("image", true, "file=" + folderInfo.get(id));
            String name=folderInfo.getName(id);
            System.out.println("图片" + name);
            if(!folderInfo.get(id).contains("福利姬")){
                sb.append("name:").append(name);
            }
            sb.append(image);
            MyProduce.setSendPicPath(name,folderInfo.get(id));
            MyProduce.sendMsg(privatemsg, sender, sb.toString(),auth);
            sb=new StringBuilder();
            if(!auth){
                return;
            }
        }
        MyProduce.sendMsg(privatemsg, sender, sb.toString(),auth);
    }

    @OnPrivate
    @Filter(value = "/help",matchType = MatchType.CONTAINS)
    public void helpMsg(PrivateMsg groupMsg, Sender sender){
        boolean onlyPrivate= groupMsg.getText().contains("private"),auth=true;
        System.out.println(groupMsg.getAccountInfo().getAccountCode()+"发送了"+groupMsg.getMsg()+"希望获得帮助");
        StringBuilder builder=new StringBuilder();
        builder.append("当前功能（[xx]为可选项）:\n");
        builder.append(" 1、“/ir [数量]“得到随机二刺猿图\n");
        builder.append(" 2、”/qr 图片“（可一次多张）识别其中可能存在的二维码链接\n");
        builder.append(" 3、“/lr [类型] [数量]“得到随机本地图片\n");
        builder.append("       类型有：ff14、ff14h（前两个奇怪的东西较多）、h、默认（图最多）\n");
        builder.append(" 4、“/help”\n");
        builder.append(" 5、”/r [x]“随机[1,x],默认x=100\n");
        builder.append(" 实在拉胯的图回复del可删除");
        builder.append(" 消息最后包含“private”则只发送私聊,没反应就是图被ban了/没给这群开");
        MyProduce.sendMsg(groupMsg,sender,builder.toString(),auth);
    }

    @OnPrivate
    @Filter(value = "/finish")
    public void closeRobot(PrivateMsg privateMsg, Sender sender){
        String s=privateMsg.getText();
        if(!privateMsg.getAccountInfo().getAccountCode().equals("1154459434")) {
            return;
        }
        MyProduce.finish();
    }
    @OnPrivate
    public void VoteNetPic(PrivateMsg privateMsg, Sender sender){
        // 获取消息正文。
        String s=privateMsg.getMsg(),res="",vote=privateMsg.getText().toLowerCase();
        int idindex=s.indexOf("localPID");
        if(!s.startsWith("[CAT:quote")&&idindex==-1) {
            return;
        }
        idindex+=8;
        while (Character.isDigit(s.charAt(idindex))){
            res+=s.charAt(idindex++);
        }
        idindex=Integer.parseInt(res);
        if(vote.indexOf("up")!=-1) {
            MyProduce.evaluation.replace(idindex, MyProduce.evaluation.get(idindex)+1);
            System.out.println(privateMsg.getAccountInfo().getAccountCode()+"对id为"+idindex+"的图片做出了+1的评价");

        } else if(vote.indexOf("down")!=-1) {
            MyProduce.evaluation.replace(idindex, MyProduce.evaluation.get(idindex)-1);
            System.out.println(privateMsg.getAccountInfo().getAccountCode()+"对id为"+idindex+"的图片做出了-1的评价");
        }
        System.out.println("图片"+idindex + "当前评价为" + MyProduce.evaluation.get(idindex));
    }
}
