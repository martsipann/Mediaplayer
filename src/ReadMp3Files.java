import java.io.File;
import java.util.ArrayList;

//See klass loeb kasutaja muusikakaustast mp3 failid sisse
public class ReadMp3Files {
	
	static ArrayList<MusicObjects> objects = new ArrayList<>();
	
	public static ArrayList<MusicObjects> extract(String c) {
		// TODO Auto-generated method stub
		String path;
		File f = new File(c);
		File l[] = f.listFiles();
		if (l == null) {
	        return objects;
	    }

		for(File x:l){
			if(x==null){
				return objects;
			}
			if(x.isHidden() || !x.canRead()){
				continue;
			}
			if(x.isDirectory()){
				objects = extract(x.getPath());
			}else if(x.getName().endsWith(".mp3")){
				path = x.getPath();
				MusicObjects mo = new MusicObjects(path.toString(), x.getName().toString());
				objects.add(mo);
			}
		}
		return objects;
	}

}
