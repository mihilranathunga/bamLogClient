package agent;

import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.*;
import static java.nio.file.LinkOption.*;
import java.nio.file.attribute.*;
import java.io.*;
import java.util.*;

/**
 * Example to watch a directory (or tree) for changes to files.
 */

public class WatchDir {

	private final WatchService watcher;
	private final Map<WatchKey, Path> keys;
	private boolean trace = false;
	/************************************/
	RandomAccessFile raf = null;
	File file = null;

	private long fileLength = 0;
	private long newFileLength;

	public static ArrayList<String> absFileList = new ArrayList<String>();
	public static ArrayList<Path> regPaths = new ArrayList<Path>();
	public static Hashtable<String, String> fileKeys = new Hashtable<String, String>();

	/**************************************/

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

				// System.out.format("%s : %s\n", name, child);

				// print out event
				// System.out.format("%s: %s\n", event.kind().name(), child);
				
				if (absFileList.contains(child.toString())) {

					readFile(child);
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

	private void readFile(Path child) {

		try {
			if (file == null) {
				System.out.println();

				file = new File(child.toString());
				raf = new RandomAccessFile(file, "r");
			}

			newFileLength = file.length();

			byte arr[] = new byte[(int) (newFileLength - fileLength)];

			raf.read(arr);
			String decoded = new String(arr, "utf-8");
			System.out.println("BAM LOG CLIENT : "+fileKeys.get(child.toString())+" - "+decoded);

			fileLength = newFileLength;

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
