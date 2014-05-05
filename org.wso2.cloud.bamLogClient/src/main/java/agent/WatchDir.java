package agent;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Example to watch a directory (or tree) for changes to files.
 */

public class WatchDir {

	private final WatchService watcher;
	private final Map<WatchKey, Path> keys;
	private boolean trace = false;

	public static ArrayList<Path> regPaths = new ArrayList<Path>();
	public static Hashtable<String, LogClientContext> readerContext = new Hashtable<String,LogClientContext>();

	@SuppressWarnings("unchecked")
	static <T> WatchEvent<T> cast(WatchEvent<?> event) {
		return (WatchEvent<T>) event;
	}

	/**
	 * Register the given directory with the WatchService
	 */
	private void register(List<Path> pathList) throws IOException {

		for (Path filePath : pathList) {
			WatchKey key = filePath.register(watcher, ENTRY_MODIFY);

			if (trace) {
				Path prev = keys.get(key);
				if (prev == null) {
					System.out.format("register: %s\n", filePath);
				} else {
					if (!filePath.equals(prev)) {
						System.out.format("update: %s -> %s\n", prev, filePath);
					}
				}
			}
			keys.put(key, filePath);
			// enable trace after initial registration
			this.trace = true;
		}

	}
	/**
	 * Creates a WatchService and registers the given directory list
	 */
	public WatchDir(List<Path> dir) throws IOException {
		this.watcher = FileSystems.getDefault().newWatchService();
		this.keys = new HashMap<WatchKey, Path>();

		register(dir);
		
	}

	/**
	 * Process all events for keys queued to the watcher
	 */
	public void processEvents() {
		
		for (;;) {
			
			// wait for key to be signalled
			WatchKey key;
			try {
				key = watcher.take();
			} catch (InterruptedException x) {
				return;
			}

			Path dir = keys.get(key);
			if (dir == null) {
				System.err.println("WatchKey not recognized!!");
				continue;
			}

			for (WatchEvent<?> event : key.pollEvents()) {
				WatchEvent.Kind kind = event.kind();

				// TBD - provide example of how OVERFLOW event is handled
				if (kind == OVERFLOW) {
					continue;
				}

				// Context for directory entry event is the file name of entry
				WatchEvent<Path> ev = cast(event);
				Path name = ev.context();
				Path child = dir.resolve(name);
				String fullFilePath = child.toString();

				// System.out.format("%s : %s\n", name, child);

				// print out event
				// System.out.format("%s: %s\n", event.kind().name(), child);
				
				if (readerContext.containsKey(fullFilePath)) {
					//System.out.println("before getting context"+readerContext.toString());
					LogClientContext context = readerContext.get(fullFilePath);
					readerContext.remove(fullFilePath);
					//System.out.println("after removing context"+readerContext.toString());
					readFile(fullFilePath,context);
				}
			}

			// reset key and remove from set if directory no longer accessible
			boolean valid = key.reset();
			if (!valid) {
				keys.remove(key);

				// all directories are inaccessible
				if (keys.isEmpty()) {
					break;
				}
			}
		}
	}

	private void readFile(String fullPath, LogClientContext context) {
		
		File file = null;

		try {
			
			if (file == null) {
				
				file = new File(fullPath);
				//raf = new RandomAccessFile(file, "r");
				
			}
			
			BufferedReader br = new BufferedReader(new FileReader(file));

			//newFileLength = file.length();
			
			//raf.getChannel().position();
			//BufferedReader br = new BufferedReader(new InputStreamReader(Channels.newInputStream(raf.getChannel())));

			//byte arr[] = new byte[(int) (newFileLength - fileLength)];

			//raf.read(arr);
			//String decoded = new String(arr, "utf-8");
			
			/*try {
	            if(!decoded.equals(null) |!decoded.equals("")){
	            	String lines[] = decoded.split("\r?\n|\r");
	            	for(String line: lines){
	            		if(!line.equals(null)| !line.equals("")){
	            			System.out.println("BAM LOG CLIENT : "+fileKeys.get(child.toString())+" - "+decoded);
	            		}
	            	}
	            
	            }
            } catch (Exception e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
            }
			fileLength = newFileLength;*/
			
			br.skip(context.getCurrentFilePointer());
			
			String line;

			while (true) {
				
				if((line = br.readLine()) == null){
					
					//System.out.println("before adding context"+readerContext.toString());
					System.out.println("Previous Point: "+context.getCurrentFilePointer()+" , Current:"+file.length());
					context.setCurrentFilePointer(file.length());
					
					try {
	                    readerContext.put(context.getAbsLogPath(), context);
                    } catch (Exception e) {
	                    // TODO Auto-generated catch block
	                    System.out.println(e.getMessage());
                    }
					//System.out.println("after adding context"+readerContext.toString()+","+context.getAbsLogPath()+" ,"+context.toString());
					break;
				}else{
					System.out.println("BAM LOG CLIENT : "+context.getFileKey()+" - "+line);
				}

			    
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
