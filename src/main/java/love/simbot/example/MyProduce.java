package love.simbot.example;

import DownloadTools.QRCode;
import catcode.CatCodeUtil;
import love.forte.common.ioc.annotation.Beans;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.message.events.PrivateMsg;
import love.forte.simbot.api.sender.Sender;

import java.io.*;
import java.net.URL;
import java.util.*;

@Beans
public class MyProduce {
	public static HashMap<Integer, Integer> evaluation = new HashMap<>();
	public static Deque<Integer> idleId = new LinkedList<>();
	public static String netPicPath = "D:\\botPic\\randomPicSave\\";
	public static HashMap<Integer, String> localPicID = new HashMap<>();
	public static int localSize = 0;
	public static HashMap<String, String> VisQR = new HashMap<>();
	private static authority groupAuth, userAuth;
	private static final HashMap<String, picFolderInfo> folder = new HashMap<>();
	private static final HashMap<String, String> nameToPath = new HashMap<>();
	private static final String DefaultMsg = "无权限";
	private static CatCodeUtil util = CatCodeUtil.INSTANCE;


	public static final String ToMoreMsg = "[CAT:image,file=D:\\botPic\\z.jpg]不可以压榨bot";
	public HashMap<String, Integer> sendedTimes = new HashMap<>();
	public HashMap<String, Boolean> warned = new HashMap<>();
	public HashMap<String, Integer> localPicSended = new HashMap<>();
	static long min = System.currentTimeMillis() / 60000;

	@Beans
	public QRCode qrCode(){
		return new QRCode().setToken();
	}

	public MyProduce() {
		HashSet<Integer> usedIndex = new HashSet<>();
		try {
			//得到已有网络图片的评价信息
			Scanner in = new Scanner(new File(netPicPath + "evaluation.txt"));
			while (in.hasNext()) {
				int index = in.nextInt(), value = in.nextInt();
				evaluation.put(index, value);
				usedIndex.add(index);
			}
			//得到剩余可用的网络图片id
			for (int i = 0; i < 2000; i++) {
				if (!usedIndex.contains(i)) {
					idleId.offerFirst(i);
				}
			}
			in.close();
			//创建几个文件夹的图片名信息
			folder.put("ff14h", new picFolderInfo("ff14h", "D:\\新建文件夹\\ff14\\h"));
			folder.put("ff14", new picFolderInfo("ff14", "D:\\新建文件夹\\ff14\\nonh"));
			folder.put("localPic", new picFolderInfo("localPic", "D:\\botPic\\pixivDownLoad"));
			folder.put("localPich", new picFolderInfo("localPich", "D:\\botPic\\pixivh"));
			folder.put("netPic", new picFolderInfo("netPic", "D:\\botPic\\randomPicSave"));
			folder.put("伪娘", new picFolderInfo("伪娘", "D:\\新建文件夹\\伪娘"));
			folder.put("福利姬", new picFolderInfo("福利姬", "D:\\新建文件夹\\福利姬"));
			folder.put("贴贴", new picFolderInfo("贴贴", "D:\\botPic\\贴贴"));
			System.err.println("图片文件夹信息读取完毕");
			groupAuth = new authority(true);
			userAuth = new authority(false);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public boolean getAuth(GroupMsg groupMsg, String level) {
		String code=groupMsg.getGroupInfo().getGroupCode(),user=groupMsg.getAccountInfo().getAccountCode();
		return groupAuth.haveAuth(code, level);
	}

	public boolean getAuth(String id, String level, boolean isUser) {
		return userAuth.haveAuth(id, level);
	}

	public picFolderInfo getFolderPath(String s) {
		return folder.get(s);
	}

	public void setSendPicPath(String name, String path) {
		nameToPath.put(name, path);
	}

	public boolean deleteByName(String name) {
		return deleteByPath(nameToPath.get(name));
	}

	private boolean deleteByPath(String s) {
		File file = new File(s);
		return file.renameTo(new File("D:\\botPic\\deletePic\\" + file.getName()));
	}

	public void finish() {
		File file = new File(netPicPath + "evaluation.txt");
		try {
			FileWriter fw = new FileWriter(file);
			fw.write("");
			fw.flush();
			evaluation.forEach((key, value) -> {
				try {
					if (key >= 0) {
						fw.write(key + "   " + value + "\n");
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
		folder.forEach((k, v) -> folder.put(k, v.refresh()));
		System.out.println("文件夹信息刷新完毕");
	}

	public void down(String url, String path) {
		File file = new File(path);
		try {
			BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
			byte[] temp = new byte[2048];
			int len = in.read(temp);
			while (len != -1) {
				out.write(temp, 0, len);
				len = in.read(temp);
			}
			in.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendMsg(GroupMsg groupMsg, Sender sender, String msg, boolean haveAuth) {
		String code = groupMsg.getGroupInfo().getGroupCode(), userID = groupMsg.getAccountInfo().getAccountCode();
		if (msg == null) {
			return;
		}

		boolean sended = warned.getOrDefault(code, false);
		int RTimes = sendedTimes.getOrDefault(code, 0);
		//check times
		if ("1154459434".equals(userID) || msg.contains("file")) {
			//可以发送
		} else if (!sended && RTimes >= 3) {//未警告且已达3次
			warned.put(code, true);
			String image = util.toCat("image", true, "file=" + "D:\\botPic\\z.jpg");
			sender.sendGroupMsg(code,image + "不可以压榨bot");
			return;
		} else if (sended) {//达五次已警告，不管
			return;
		}

		if(!msg.contains("file")&&!msg.equals("")){
			RTimes++;
			sendedTimes.put(code, RTimes);
		}

		System.out.println("群" + code + "当前一分钟内发言次数" + RTimes);
		if (("1154459434".equals(userID) || haveAuth)&&msg.length()>0) {
			sender.sendGroupMsg(code, msg);
		} else if (!haveAuth) {
			sender.sendGroupMsg(code, DefaultMsg);
		}
	}

	public static void sendMsg(PrivateMsg privateMsg, Sender sender, String msg, boolean haveAuth) {
		String userID = privateMsg.getAccountInfo().getAccountCode();
		if (!haveAuth) {
			sender.sendPrivateMsg(userID, DefaultMsg);
			return;
		}
		sender.sendPrivateMsg(userID, msg);
	}
}