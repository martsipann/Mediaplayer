import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Port;
import javax.sound.sampled.Port.Info;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;


@SuppressWarnings("serial")
public class CreateWindow extends JFrame implements Runnable {

	public static ArrayList<MusicObjects> objects = new ArrayList<>();
	public static AdvancedPlayer p;
	public static int isPlaying;
	public static int index; //Selle j�rgi v�tan listist laule
	public static int listIndex; //�he asja lihtsamaks tegemiseks kasutan kahte indexi muutujat
	public static boolean loop = true;
	public static JList<Object> songs;
	public static boolean stopSong;//While ts�kli peatamiseks, kui valitakse listist uus laul
	
	public static void main(String[] args) {
			//Akna tegemiseks vajalikud p�hiasjad
			String user = System.getProperty("user.name");//Et leida muusika kaust
			final DefaultListModel<String> listModel = new DefaultListModel<String>();
			final JFrame frame = new JFrame("Music player");
			final JPanel musicPanel = new JPanel(new GridBagLayout());
			JPanel playPanel = new JPanel(new GridBagLayout());
			JPanel audioPanel = new JPanel(new GridBagLayout());
			JPanel filePanel = new JPanel(new GridBagLayout());
			final JFileChooser fChooser = new JFileChooser();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(900, 700);
			
			//Loeb k�ik helifailid listi
			
			//Loeb kaustast andmed sisse
			objects = ReadMp3Files.extract("c:\\Users\\" + user + "\\music");
			if(!objects.isEmpty()){
			for (MusicObjects i : objects){
				listModel.addElement(i.name);
			}
			}
			//Teeb listviewi ja lisab scrollbari juurde ning lisaks k�ik front endiga seotud k�sud
			songs = new JList(listModel);
			songs.setSelectionBackground(Color.RED);
			
			//Kui vajutad listis olevale muusikale, siis esimesel klikil salvestab asukoha ja teisel k�ivitab
			songs.addMouseListener(new MouseAdapter(){
				 @SuppressWarnings("unchecked")
				public void mouseClicked(MouseEvent evt) {
				        if (evt.getButton() == MouseEvent.BUTTON1){
							JList<Object> list = (JList<Object>)evt.getSource();
							if(evt.getClickCount() == 1){
						       listIndex = list.locationToIndex(evt.getPoint());
							}
					        if (evt.getClickCount() == 2) {	
					        	if(p != null){
					        		stopSong = true;
					        	}else{
					        		stopSong = false;
					        	}
					        	index = listIndex;
					            startMusic();
					        }
				        }
				 }

			});
			
			final JScrollPane scroll = new JScrollPane(songs);;
			scroll.setPreferredSize(new Dimension(500, 500));
			musicPanel.add(scroll);
			
			//Nuppude tegemine ja lisamine
			
			JButton play = makeButton("Play");
			play.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent arg0) {
					if(p != null){
		        		stopSong = true;
		        	}else{
		        		stopSong = false;
		        	}
		        	index = listIndex;
		            System.out.println(objects.get(index).path);
					startMusic();
				}
				
			});
			
			JButton stop = makeButton("Stop");
			stop.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent arg0) {
					// TODO Auto-generated method stub
					stopSong = true;
					stopMusic();
				}
				
			});

			playPanel.add(play);
			playPanel.add(stop);
			setVolume(5);//Default volume sound
			JSlider slider = new JSlider();
			slider.setMaximum(10);
			slider.setMinimum(0);
			slider.setValue(5);
			slider.addChangeListener(new ChangeListener(){

				@Override
				public void stateChanged(ChangeEvent c) {
					setVolume(((JSlider)c.getSource()).getValue());
				}
				
			});
			
			audioPanel.add(slider);
			
			JButton addMusic = makeButton("Add Music");
			addMusic.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					int returnVal = fChooser.showOpenDialog(frame);
					if(returnVal == JFileChooser.APPROVE_OPTION){
						File file = fChooser.getSelectedFile();
						if(file.getName().endsWith(".mp3")){
							MusicObjects mo = new MusicObjects(file.getPath().toString(), file.getName().toString());
							objects.add(mo);
							listModel.addElement(mo.name);		
							System.out.println("File: " + mo.name);
							System.out.println("lm: " + listModel.get(0));
						}else{
							JOptionPane.showMessageDialog(frame, "See ei ole mp3 fail");
						}
					}
				}
				
			});
			
			filePanel.add(addMusic);
			
			frame.add(musicPanel, BorderLayout.WEST);
			frame.add(playPanel, BorderLayout.SOUTH);
			frame.add(audioPanel, BorderLayout.EAST);
			frame.add(filePanel, BorderLayout.NORTH);
			frame.setVisible(true);
	}
	
	private static JButton makeButton(String text){
		JButton b = new JButton();
		b.setText(text);
		b.setPreferredSize(new Dimension(100, 50));
		
		return b;
	}
	private static void stopMusic() {
		p.close();
		isPlaying = 0;
	}
	
	private static void playMusic(){
		try {
			while(loop){
				if(index < 0 || index >= objects.size()){
					break;
				}
				String path = objects.get(index).path;
				songs.setSelectedIndex(index);
				FileInputStream song = new FileInputStream(path);
				isPlaying = 1;
				p = new AdvancedPlayer(song);
				p.play();	
				TimeUnit.SECONDS.sleep(1);
				if(stopSong){
					stopSong = false;
					break;
				}
				if(index++ == objects.size()){//listi l�pp
					break;
				}
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JavaLayerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
	private static void startMusic() {
		new Thread(new Runnable(){
        	public void run(){
        		if(isPlaying == 1){
        			stopMusic();
        		}
        		playMusic();	
        	}
        }).start();
	}

	private static void setVolume(float vol){
		Info source = Port.Info.SPEAKER;
		if(AudioSystem.isLineSupported(source)){
			try{
				Port outline = (Port) AudioSystem.getLine(source);
				outline.open();
				FloatControl volumeControl = (FloatControl) outline.getControl(FloatControl.Type.VOLUME);
				float v = vol/10;
				volumeControl.setValue(v);
			}catch(LineUnavailableException ex){
				
			}
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
