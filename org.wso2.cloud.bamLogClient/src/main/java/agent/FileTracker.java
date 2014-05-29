
/*
 * Copyright (c) 2008, 2010, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class keeps track of provided log files
 */

public class FileTracker {

	private final WatchService watcher;
	private final Map<WatchKey, Path> keys;
	private boolean trace = false;

	public static ArrayList<Path> regPaths = new ArrayList<Path>();
	private static Hashtable<String, LogClientContext> readerContext = new Hashtable<String, LogClientContext>();

	private static final Log log = LogFactory.getLog(FileTracker.class);

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
					log.info(String.format("register: %s\n", filePath));
				} else {
					if (!filePath.equals(prev)) {
						log.info(String.format("update: %s -> %s\n", prev, filePath));
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
	public FileTracker(List<Path> dir) throws IOException {
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
				log.error("WatchKey not recognized!!");
				continue;
			}

			for (WatchEvent<?> event : key.pollEvents()) {
				WatchEvent.Kind kind = event.kind();
				
				// Context for directory entry event is the file name of entry
				WatchEvent<Path> ev = cast(event);
				Path name = ev.context();
				Path child = dir.resolve(name);
				String fullFilePath = child.toString();

				// TBD - provide example of how OVERFLOW event is handled
				if (kind == OVERFLOW) {
					log.error(" Some events might have been discarded, overflow has occured while reading \""+fullFilePath+"\"");
					continue;
				}
				// print out event
				log.debug(String.format("%s: %s %s\n", name, event.kind().name(),child));

				if (readerContext.containsKey(fullFilePath)) {

					LogClientContext context = getContextFromList(fullFilePath);
					readFile(fullFilePath, context);
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

	@SuppressWarnings("resource")
	private void readFile(String fullPath, LogClientContext context) {

		File file = null;

		try {
			if (file == null) {
				file = new File(fullPath);
			}

			BufferedReader br = new BufferedReader(new FileReader(file));

			br.skip(context.getCurrentFilePointer());

			String line;

			while (true) {

				if ((line = br.readLine()) == null) {

					log.debug("Previous Point: " + context.getCurrentFilePointer());
					context.setCurrentFilePointer(file.length());
					log.debug("Current Point: " + context.getCurrentFilePointer());

					putContextToList(context.getAbsLogPath(), context);

					break;
				} else {
					String aLog = context.getFileKey() + " - " + line;
					log.info(aLog);

					BamDataSender.publishLogEvents(context,
					                               line,
					                               new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SZ").format(new Date()));
				}

			}

		} catch (NullPointerException e){
			log.error("Null Exception in reading file - "+e.getMessage(), e);
		} catch (IOException e) {
	        log.error("Error reading file - "+e.getMessage(),e);
        }
	}
	/**
	 * This method will return a context object from context list
	 * @param absLogFilePath
	 * @return
	 */
	public static LogClientContext getContextFromList(String absLogFilePath){
		LogClientContext context = null;
        try {
	        log.debug("Before getting context :" +readerContext.toString());
	        context = readerContext.get(absLogFilePath);
	        readerContext.remove(absLogFilePath);
	        log.debug("After getting context :" +readerContext.toString());
        } catch (Exception e) {
	        log.error("Exception occured getting context object from context list - "+e.getMessage(), e);
        }
		return context;
	}
	public static boolean putContextToList(String absLogFilePath, LogClientContext context){
		boolean status = false; 
		try {
	        log.debug("Before adding context :" +readerContext.toString());
	        readerContext.put(absLogFilePath, context);
	        log.debug("After adding context :" +readerContext.toString());
	        status = true;
        } catch (Exception e) {
        	log.error("Exception occured putting context object to context list - "+e.getMessage(), e);
        }
		return status;
	}
}
