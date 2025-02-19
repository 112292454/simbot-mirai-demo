package love.simbot.example.service;

import com.huaban.analysis.jieba.JiebaSegmenter;
import jakarta.annotation.Resource;
import love.simbot.example.component.MessageDivision;
import love.simbot.example.component.MyWordFrequency;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MessagePersistenceTemplate {
	HashMap<String, MessageDivision> template=new HashMap<>();
	JiebaSegmenter segmenter = new JiebaSegmenter();
	@Resource
	RedisTemplate<String,MessageDivision> redisTemplate;
	static Pattern removePunct=Pattern.compile("[,./;'\\[\\]\\\\=_——$￥%<>?:\"{}|}\\]，」。、\n；‘【】、《》？：“”（）()……·`~]");


	public void putMessage(String code,String s){
		Matcher matcher = removePunct.matcher(s);
		s=matcher.replaceAll(" ").trim();
		List<String> words=segmenter.sentenceProcess(s);
		System.out.println("words = " + words);
		if(!template.containsKey(code)) {
			template.put(code,new MessageDivision());
		}
		MessageDivision division=template.get(code);
		for (String word : words) {
			if(word.length()<2|| !StringUtils.hasText(word)) continue;
			try {
				Integer.parseInt(word);
				continue;
			} catch (NumberFormatException e) { }

			int time=division.getTemp().getOrDefault(word,0);
			division.getTemp().put(word,time+1);
		}
	}
	public void persistence(){
		ValueOperations<String,MessageDivision> ops=redisTemplate.opsForValue();
		for (Map.Entry<String, MessageDivision> e : template.entrySet()) {
			String code=e.getKey(),date=new SimpleDateFormat("yyyy-MM-dd").format(new Date());

			MessageDivision division=e.getValue();
			if(division.getTemp().size()==0) continue;
			MessageDivision other= null;
			try {
				other = ops.get(code+"|"+date);
			} catch (Exception exception) {
				exception.printStackTrace();
			}
			if(other==null) {
				other=new MessageDivision();
				other.setTemp(new HashMap<String, Integer>());
				other.setDivision(new ArrayList<MyWordFrequency>());
			}


			division.mergeDivision(other.getDivision());
			division.templateChange();

			ops.set(code+"|"+date,division);

			division.clear();

		}
	}

}
