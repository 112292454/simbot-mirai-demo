package love.simbot.example;

import catcode.CatCodeUtil;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.message.events.PrivateMsg;
import love.forte.simbot.api.sender.Sender;

import java.io.*;
import java.net.URL;
import java.util.*;

public class MyProduce {
	public static HashMap<Integer,Integer> evaluation=new HashMap<>();
	public static Deque<Integer> idleId=new LinkedList<>();
	public static String netPicPath ="D:\\botPic\\randomPicSave\\";
	public static HashMap<Integer,String> localPicID=new HashMap<>();
	public static int localSize=0;
	public static HashMap<String,String> VisQR=new HashMap<>();
	private static  authority groupAuth,userAuth;
	private static final HashMap<String,picFolderInfo> folder=new HashMap<>();
	private static final HashMap<String,String> nameToPath=new HashMap<>();
	private static final String DefaultMsg="无权限";

	private static HashMap<String,Integer> sendedTimes=new HashMap<>();
	private static HashMap<String,Boolean> warned=new HashMap<>();
	static long min=System.currentTimeMillis()/60000;

	public MyProduce(){
		HashSet<Integer> usedIndex=new HashSet<>();
		try {
			//得到已有网络图片的评价信息
			Scanner in=new Scanner(new File(netPicPath +"evaluation.txt"));
			while (in.hasNext()){
				int index=in.nextInt(),value=in.nextInt();
				evaluation.put(index,value);
				usedIndex.add(index);
			}
			//得到剩余可用的网络图片id
			for (int i = 0; i < 2000; i++) {
				if(!usedIndex.contains(i)) {idleId.offerFirst(i);}
			}
			in.close();
			//创建几个文件夹的图片名信息
			folder.put("ff14h",new picFolderInfo("D:\\新建文件夹\\ff14\\h"));
			folder.put("ff14",new picFolderInfo("D:\\新建文件夹\\ff14\\nonh"));
			folder.put("localPic",new picFolderInfo("D:\\botPic\\pixivDownLoad"));
			folder.put("localPich",new picFolderInfo("D:\\botPic\\pixivh"));
			folder.put("netPic",new picFolderInfo("D:\\botPic\\randomPicSave"));
			folder.put("伪娘",new picFolderInfo("D:\\新建文件夹\\伪娘"));
			folder.put("福利姬",new picFolderInfo("D:\\新建文件夹\\福利姬"));
			folder.put("贴贴",new picFolderInfo("D:\\botPic\\贴贴"));
			System.err.println("图片文件夹信息读取完毕");
			groupAuth=new authority(true);
			userAuth=new authority(false);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	public static boolean getAuth(String id,String level){
		return groupAuth.haveAuth(id,level);
	}
	public static boolean getAuth(String id,String level,boolean isUser){
		return userAuth.haveAuth(id,level);
	}
	public static picFolderInfo getFolderPath(String s){
		return folder.get(s);
	}
	public static void setSendPicPath(String name,String path){
		nameToPath.put(name,path);
	}
	public static boolean deleteByName(String name){
		return deleteByPath(nameToPath.get(name));
	}
	private static boolean deleteByPath(String s){
		File file=new File(s);
		return file.renameTo(new File("D:\\botPic\\deletePic\\"+file.getName()));
	}
	public static void finish(){
		File file=new File(netPicPath +"evaluation.txt");
		try {
			FileWriter fw=new FileWriter(file);
			fw.write("");
			fw.flush();
			evaluation.forEach((key,value)->{
				try {
					if(key>=0) {
						fw.write(key+"   "+value+"\n");
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			System.out.println("评分信息保存完毕");
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		folder.forEach((k,v)->folder.put(k,v.refresh()));
		System.out.println("文件夹信息刷新完毕");
	}
	public static void down(String url,String path){
		File file = new File(path);
		try {
			BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
			byte[] temp=new byte[2048];
			int len=in.read(temp);
			while(len!=-1){
				out.write(temp,0,len);
				len=in.read(temp);
			}
			in.close();out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void sendMsg(GroupMsg groupMsg, Sender sender, String msg,boolean haveAuth){
		String gruopID=groupMsg.getGroupInfo().getGroupCode(),userID=groupMsg.getAccountInfo().getAccountCode();
		if(msg==null|| "".equals(msg)){
			return;
		}
		CatCodeUtil util = CatCodeUtil.INSTANCE;

		boolean sended=warned.getOrDefault(gruopID,false);
		int RTimes=sendedTimes.getOrDefault(gruopID,0);

		//check times
		if(System.currentTimeMillis()/60000-min<2){
			if("1154459434".equals(userID)||msg.contains("file")){
				//可以发送
			}else if(sended&&RTimes>3){//未警告且已达3次
				String image = util.toCat("image", true, "file=" +"D:\\botPic\\z.jpg");
				MyProduce.sendMsg(groupMsg,sender,image+"不可以压榨bot",true);
				warned.put(gruopID,true);
				return;
			}else if(RTimes>=5){//达五次已警告，不管
				return;
			}
		}else{//新的一分钟，重置
			min=System.currentTimeMillis()/60000;
			warned.put(gruopID,false);
			sendedTimes.put(gruopID,0);

		}
		sendedTimes.put(gruopID,RTimes+1);
		System.out.println("当前次数"+RTimes);
		if("1154459434".equals(userID)){
			sender.sendGroupMsg(gruopID,msg);
		}
		else if(!haveAuth){
			sender.sendGroupMsg(gruopID,DefaultMsg);
		}
		else  {
			sender.sendGroupMsg(gruopID,msg);
		}
	}
	public static void sendMsg(PrivateMsg privateMsg, Sender sender, String msg, boolean haveAuth){
		String userID=privateMsg.getAccountInfo().getAccountCode();
		if(!haveAuth){
			sender.sendPrivateMsg(userID,DefaultMsg);
			return;
		}
		sender.sendPrivateMsg(userID,msg);
	}
}