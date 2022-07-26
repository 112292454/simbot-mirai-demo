package love.simbot.example;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Scanner;
import java.util.stream.Stream;

public class picFolderInfo {
	private final HashMap<Integer,String> PicPath;
	private final String folder;
	private int nums=0;
	private String kind;
	public picFolderInfo(String kind,String folderPath) {
		folder=folderPath;
		this.kind=kind;
		try {
			new BatchController().batch(folderPath,kind);
		} catch (Exception e) {
			e.printStackTrace();
		}


		File info = new File(folderPath + "\\info.txt");
		PicPath = new HashMap<>();
		try {
			if (info.exists()) {
				//如果info已经存在
				Scanner in = new Scanner(info);
				while (in.hasNext()){
					//读入存map即可
					PicPath.put(in.nextInt(),in.nextLine().trim());
				}
			}else{
				//若info不存在
				File floder = new File(folderPath);
				if(!info.createNewFile()) {
					System.err.println(folderPath + "的info创建失败!");
				}

				//对所有图片类型存入
				dfs(folderPath);

				//信息写入info
				FileWriter fw=new FileWriter(info);
				PicPath.forEach((k, v)->{
					try {
						fw.write(k+" "+v+"\n");
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
				fw.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void dfs(String p) {
		PicPath.clear();
		try {
			Path path=Paths.get(new File(p).toURI());
			Stream<Path> walk= Files.walk(path);
			walk=walk.filter(t->{
				String s=t.getFileName().toString().toLowerCase(Locale.ROOT);
				return s.endsWith("jpg")||s.endsWith("png")||s.endsWith("bmp");
			});
			walk.forEach(t->{
				if(t.toFile().isFile()) {
					PicPath.put(nums++,t.toAbsolutePath().toString());
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public  String get(int i){
		return PicPath.getOrDefault(i,null);
	}
	public  String getName(int i){
		String[] path= PicPath.get(i).split("\\\\|\\.");
		return path[path.length-2];
	}
	public int size(){
		return PicPath.size();
	}
	public picFolderInfo refresh(){
		try {
			Files.delete(new File(folder + "\\info.txt").toPath());
		} catch (IOException e) {
			e.printStackTrace();
			return this;
		}
		return new picFolderInfo(folder, folder);
	}
}
