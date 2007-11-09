/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.widgets;

import org.eclipse.swt.internal.Callback;
import org.eclipse.swt.internal.cocoa.*;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

/**
 * Instances of this class are responsible for managing the
 * connection between SWT and the underlying operating
 * system. Their most important function is to implement
 * the SWT event loop in terms of the platform event model.
 * They also provide various methods for accessing information
 * about the operating system, and have overall control over
 * the operating system resources which SWT allocates.
 * <p>
 * Applications which are built with SWT will <em>almost always</em>
 * require only a single display. In particular, some platforms
 * which SWT supports will not allow more than one <em>active</em>
 * display. In other words, some platforms do not support
 * creating a new display if one already exists that has not been
 * sent the <code>dispose()</code> message.
 * <p>
 * In SWT, the thread which creates a <code>Display</code>
 * instance is distinguished as the <em>user-interface thread</em>
 * for that display.
 * </p>
 * The user-interface thread for a particular display has the
 * following special attributes:
 * <ul>
 * <li>
 * The event loop for that display must be run from the thread.
 * </li>
 * <li>
 * Some SWT API methods (notably, most of the public methods in
 * <code>Widget</code> and its subclasses), may only be called
 * from the thread. (To support multi-threaded user-interface
 * applications, class <code>Display</code> provides inter-thread
 * communication methods which allow threads other than the 
 * user-interface thread to request that it perform operations
 * on their behalf.)
 * </li>
 * <li>
 * The thread is not allowed to construct other 
 * <code>Display</code>s until that display has been disposed.
 * (Note that, this is in addition to the restriction mentioned
 * above concerning platform support for multiple displays. Thus,
 * the only way to have multiple simultaneously active displays,
 * even on platforms which support it, is to have multiple threads.)
 * </li>
 * </ul>
 * Enforcing these attributes allows SWT to be implemented directly
 * on the underlying operating system's event model. This has 
 * numerous benefits including smaller footprint, better use of 
 * resources, safer memory management, clearer program logic,
 * better performance, and fewer overall operating system threads
 * required. The down side however, is that care must be taken
 * (only) when constructing multi-threaded applications to use the
 * inter-thread communication mechanisms which this class provides
 * when required.
 * </p><p>
 * All SWT API methods which may only be called from the user-interface
 * thread are distinguished in their documentation by indicating that
 * they throw the "<code>ERROR_THREAD_INVALID_ACCESS</code>"
 * SWT exception.
 * </p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>(none)</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Close, Dispose, Settings</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 * @see #syncExec
 * @see #asyncExec
 * @see #wake
 * @see #readAndDispatch
 * @see #sleep
 * @see Device#dispose
 */
public class Display extends Device {
	
	/* Windows and Events */
	Event [] eventQueue;
	EventTable eventTable, filterTable;
	boolean disposing;

	/* Sync/Async Widget Communication */
	Synchronizer synchronizer = new Synchronizer (this);
	Thread thread;
	
	NSApplication application;
	NSAutoreleasePool pool;

	NSPoint cascade = new NSPoint();

	Callback windowDelegateCallback2, windowDelegateCallback3, windowDelegateCallback4, windowDelegateCallback5;
	Callback dialogCallback3;
	
	/* Menus */
//	Menu menuBar;
//	Menu [] menus, popups;
//	static final int ID_TEMPORARY = 1000;
//	static final int ID_START = 1001;
	
	/* Display Shutdown */
	Runnable [] disposeList;

	/* System Tray */
	Tray tray;
	
	/* System Resources */
	Image errorImage, infoImage, warningImage;
	Cursor [] cursors = new Cursor [SWT.CURSOR_HAND + 1];
	
	/* Key Mappings. */
	static int [] [] KeyTable = {

		/* Keyboard and Mouse Masks */
//		{58,	SWT.ALT},
//		{56,	SWT.SHIFT},
//		{59,	SWT.CONTROL},
//		{55,	SWT.COMMAND},

		/* Non-Numeric Keypad Keys */
		{OS.NSUpArrowFunctionKey, SWT.ARROW_UP},
//		{125, SWT.ARROW_DOWN},
//		{123, SWT.ARROW_LEFT},
//		{124, SWT.ARROW_RIGHT},
//		{116, SWT.PAGE_UP},
//		{121, SWT.PAGE_DOWN},
//		{115, SWT.HOME},
//		{119, SWT.END},
		
//		{??,	SWT.INSERT},

		/* Virtual and Ascii Keys */
//		{51,	SWT.BS},
//		{36,	SWT.CR},
//		{117, SWT.DEL},
//		{53,	SWT.ESC},
//		{76,	SWT.LF},
//		{48,	SWT.TAB},	
		
		/* Functions Keys */
//		{122, SWT.F1},
//		{120, SWT.F2},
//		{99,	SWT.F3},
//		{118, SWT.F4},
//		{96,	SWT.F5},
//		{97,	SWT.F6},
//		{98,	SWT.F7},
//		{100, SWT.F8},
//		{101, SWT.F9},
//		{109, SWT.F10},
//		{103, SWT.F11},
//		{111, SWT.F12},
//		{105, SWT.F13},
//		{107, SWT.F14},
//		{113, SWT.F15},
		
		/* Numeric Keypad Keys */
//		{67, SWT.KEYPAD_MULTIPLY},
//		{69, SWT.KEYPAD_ADD},
//		{76, SWT.KEYPAD_CR},
//		{78, SWT.KEYPAD_SUBTRACT},
//		{65, SWT.KEYPAD_DECIMAL},
//		{75, SWT.KEYPAD_DIVIDE},
//		{82, SWT.KEYPAD_0},
//		{83, SWT.KEYPAD_1},
//		{84, SWT.KEYPAD_2},
//		{85, SWT.KEYPAD_3},
//		{86, SWT.KEYPAD_4},
//		{87, SWT.KEYPAD_5},
//		{88, SWT.KEYPAD_6},
//		{89, SWT.KEYPAD_7},
//		{91, SWT.KEYPAD_8},
//		{92, SWT.KEYPAD_9},
//		{81, SWT.KEYPAD_EQUAL},

		/* Other keys */
//		{??,	SWT.CAPS_LOCK},
		
//		{71,	SWT.NUM_LOCK},
		
//		{??,	SWT.SCROLL_LOCK},
//		{??,	SWT.PAUSE},
//		{??,	SWT.BREAK},
//		{??,	SWT.PRINT_SCREEN},
		
//		{114, SWT.HELP},
		
	};

	static String APP_NAME = "SWT";
	static final String ADD_WIDGET_KEY = "org.eclipse.swt.internal.addWidget";

	/* Multiple Displays. */
	static Display Default;
	static Display [] Displays = new Display [4];
				
	/* Package Name */
	static final String PACKAGE_PREFIX = "org.eclipse.swt.widgets.";
			
	/* Display Data */
	Object data;
	String [] keys;
	Object [] values;
	
	/*
	* TEMPORARY CODE.  Install the runnable that
	* gets the current display. This code will
	* be removed in the future.
	*/
	static {
		DeviceFinder = new Runnable () {
			public void run () {
				Device device = getCurrent ();
				if (device == null) {
					device = getDefault ();
				}
				setDevice (device);
			}
		};
	}
	
/*
* TEMPORARY CODE.
*/
static void setDevice (Device device) {
	CurrentDevice = device;
}

static byte [] ascii (String name) {
	int length = name.length ();
	char [] chars = new char [length];
	name.getChars (0, length, chars, 0);
	byte [] buffer = new byte [length + 1];
	for (int i=0; i<length; i++) {
		buffer [i] = (byte) chars [i];
	}
	return buffer;
}

static int translateKey (int key) {
	for (int i=0; i<KeyTable.length; i++) {
		if (KeyTable [i] [0] == key) return KeyTable [i] [1];
	}
	return 0;
}

static int untranslateKey (int key) {
	for (int i=0; i<KeyTable.length; i++) {
		if (KeyTable [i] [1] == key) return KeyTable [i] [0];
	}
	return 0;
}

/**
 * Adds the listener to the collection of listeners who will
 * be notified when an event of the given type occurs anywhere
 * in a widget. The event type is one of the event constants
 * defined in class <code>SWT</code>. When the event does occur,
 * the listener is notified by sending it the <code>handleEvent()</code>
 * message.
 * <p>
 * Setting the type of an event to <code>SWT.None</code> from
 * within the <code>handleEvent()</code> method can be used to
 * change the event type and stop subsequent Java listeners
 * from running. Because event filters run before other listeners,
 * event filters can both block other listeners and set arbitrary
 * fields within an event. For this reason, event filters are both
 * powerful and dangerous. They should generally be avoided for
 * performance, debugging and code maintenance reasons.
 * </p>
 * 
 * @param eventType the type of event to listen for
 * @param listener the listener which should be notified when the event occurs
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 *
 * @see Listener
 * @see SWT
 * @see #removeFilter
 * @see #removeListener
 * 
 * @since 3.0 
 */
public void addFilter (int eventType, Listener listener) {
	checkDevice ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (filterTable == null) filterTable = new EventTable ();
	filterTable.hook (eventType, listener);
}

/**
 * Adds the listener to the collection of listeners who will
 * be notified when an event of the given type occurs. The event
 * type is one of the event constants defined in class <code>SWT</code>.
 * When the event does occur in the display, the listener is notified by
 * sending it the <code>handleEvent()</code> message.
 *
 * @param eventType the type of event to listen for
 * @param listener the listener which should be notified when the event occurs
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 *
 * @see Listener
 * @see SWT
 * @see #removeListener
 * 
 * @since 2.0 
 */
public void addListener (int eventType, Listener listener) {
	checkDevice ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (eventTable == null) eventTable = new EventTable ();
	eventTable.hook (eventType, listener);
}

//void addMenu (Menu menu) {
//	if (menus == null) menus = new Menu [12];
//	for (int i=0; i<menus.length; i++) {
//		if (menus [i] == null) {
//			menu.id = (short)(ID_START + i);
//			menus [i] = menu;
//			return;
//		}
//	}
//	Menu [] newMenus = new Menu [menus.length + 12];
//	menu.id = (short)(ID_START + menus.length);
//	newMenus [menus.length] = menu;
//	System.arraycopy (menus, 0, newMenus, 0, menus.length);
//	menus = newMenus;
//}
//
//void addPopup (Menu menu) {
//	if (popups == null) popups = new Menu [4];
//	int length = popups.length;
//	for (int i=0; i<length; i++) {
//		if (popups [i] == menu) return;
//	}
//	int index = 0;
//	while (index < length) {
//		if (popups [index] == null) break;
//		index++;
//	}
//	if (index == length) {
//		Menu [] newPopups = new Menu [length + 4];
//		System.arraycopy (popups, 0, newPopups, 0, length);
//		popups = newPopups;
//	}
//	popups [index] = menu;
//}

/**
 * Causes the <code>run()</code> method of the runnable to
 * be invoked by the user-interface thread at the next 
 * reasonable opportunity. The caller of this method continues 
 * to run in parallel, and is not notified when the
 * runnable has completed.  Specifying <code>null</code> as the
 * runnable simply wakes the user-interface thread when run.
 * <p>
 * Note that at the time the runnable is invoked, widgets 
 * that have the receiver as their display may have been
 * disposed. Therefore, it is necessary to check for this
 * case inside the runnable before accessing the widget.
 * </p>
 *
 * @param runnable code to run on the user-interface thread or <code>null</code>
 *
 * @exception SWTException <ul>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 * 
 * @see #syncExec
 */
public void asyncExec (Runnable runnable) {
	synchronized (Device.class) {
		if (isDisposed ()) error (SWT.ERROR_DEVICE_DISPOSED);
		synchronizer.asyncExec (runnable);
	}
}

/**
 * Causes the system hardware to emit a short sound
 * (if it supports this capability).
 * 
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public void beep () {
	checkDevice ();
}

protected void checkDevice () {
	if (thread == null) error (SWT.ERROR_WIDGET_DISPOSED);
	if (thread != Thread.currentThread ()) error (SWT.ERROR_THREAD_INVALID_ACCESS);
	if (isDisposed ()) error (SWT.ERROR_DEVICE_DISPOSED);
}

/**
 * Checks that this class can be subclassed.
 * <p>
 * IMPORTANT: See the comment in <code>Widget.checkSubclass()</code>.
 * </p>
 *
 * @exception SWTException <ul>
 *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
 * </ul>
 *
 * @see Widget#checkSubclass
 */
protected void checkSubclass () {
	if (!Display.isValidClass (getClass ())) error (SWT.ERROR_INVALID_SUBCLASS);
}

/**
 * Constructs a new instance of this class.
 * <p>
 * Note: The resulting display is marked as the <em>current</em>
 * display. If this is the first display which has been 
 * constructed since the application started, it is also
 * marked as the <em>default</em> display.
 * </p>
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if called from a thread that already created an existing display</li>
 *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
 * </ul>
 *
 * @see #getCurrent
 * @see #getDefault
 * @see Widget#checkSubclass
 * @see Shell
 */
public Display () {
	this (null);
}

/**
 * Constructs a new instance of this class using the parameter.
 * 
 * @param data the device data
 */
public Display (DeviceData data) {
	super (data);
}

static void checkDisplay (Thread thread, boolean multiple) {
	synchronized (Device.class) {
		for (int i=0; i<Displays.length; i++) {
			if (Displays [i] != null) {
				if (!multiple) SWT.error (SWT.ERROR_NOT_IMPLEMENTED, null, " [multiple displays]");
				if (Displays [i].thread == thread) SWT.error (SWT.ERROR_THREAD_INVALID_ACCESS);
			}
		}
	}
}

static String convertToLf(String text) {
	char Cr = '\r';
	char Lf = '\n';
	int length = text.length ();
	if (length == 0) return text;
	
	/* Check for an LF or CR/LF.  Assume the rest of the string 
	 * is formated that way.  This will not work if the string 
	 * contains mixed delimiters. */
	int i = text.indexOf (Lf, 0);
	if (i == -1 || i == 0) return text;
	if (text.charAt (i - 1) != Cr) return text;

	/* The string is formatted with CR/LF.
	 * Create a new string with the LF line delimiter. */
	i = 0;
	StringBuffer result = new StringBuffer ();
	while (i < length) {
		int j = text.indexOf (Cr, i);
		if (j == -1) j = length;
		String s = text.substring (i, j);
		result.append (s);
		i = j + 2;
		result.append (Lf);
	}
	return result.toString ();
}

/**
 * Requests that the connection between SWT and the underlying
 * operating system be closed.
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 *
 * @see Device#dispose
 * 
 * @since 2.0
 */
public void close () {
	checkDevice ();
	Event event = new Event ();
	sendEvent (SWT.Close, event);
	if (event.doit) dispose ();
}

/**
 * Creates the device in the operating system.  If the device
 * does not have a handle, this method may do nothing depending
 * on the device.
 * <p>
 * This method is called before <code>init</code>.
 * </p>
 *
 * @param data the DeviceData which describes the receiver
 *
 * @see #init
 */
protected void create (DeviceData data) {
	checkSubclass ();
	checkDisplay (thread = Thread.currentThread (), false);
	createDisplay (data);
	register (this);
	if (Default == null) Default = this;
}

void createDisplay (DeviceData data) {
	if (OS.VERSION < 0x1040) {
		System.out.println ("***WARNING: SWT requires MacOS X version " + 10 + "." + 4 + " or greater"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		System.out.println ("***WARNING: Detected: " + Integer.toHexString((OS.VERSION & 0xFF00) >> 8) + "." + Integer.toHexString((OS.VERSION & 0xF0) >> 4) + "." + Integer.toHexString(OS.VERSION & 0xF)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/*
	* Feature in the Macintosh.  On OS 10.2, it is necessary
	* to explicitly check in with the Process Manager and set
	* the current process to be the front process in order for
	* windows to come to the front by default.  The fix is call
	* both GetCurrentProcess() and SetFrontProcess().
	* 
	* NOTE: It is not actually necessary to use the process
	* serial number returned by GetCurrentProcess() in the
	* call to SetFrontProcess() (ie. kCurrentProcess can be
	* used) but both functions must be called in order for
	* windows to come to the front.
	*/
	int [] psn = new int [2];
	if (OS.GetCurrentProcess (psn) == OS.noErr) {
//		int pid = OS.getpid ();
//		byte [] buffer = null;
//		int ptr = OS.getenv (ascii ("APP_NAME_" + pid));
//		if (ptr != 0) {
//			buffer = new byte [OS.strlen (ptr) + 1];
//			OS.memmove (buffer, ptr, buffer.length);
//		} else {
//			if (APP_NAME != null) {
//				char [] chars = new char [APP_NAME.length ()];
//				APP_NAME.getChars (0, chars.length, chars, 0);
//				int cfstring = OS.CFStringCreateWithCharacters (OS.kCFAllocatorDefault, chars, chars.length);
//				if (cfstring != 0) {
//					CFRange range = new CFRange ();
//					range.length = chars.length;
//					int encoding = OS.CFStringGetSystemEncoding ();
//					int [] size = new int [1];
//					int numChars = OS.CFStringGetBytes (cfstring, range, encoding, (byte) '?', true, null, 0, size);
//					if (numChars != 0) {
//						buffer = new byte [size [0] + 1];
//						numChars = OS.CFStringGetBytes (cfstring, range, encoding, (byte) '?', true, buffer, size [0], size);
//					}
//					OS.CFRelease (cfstring);
//				}
//			}
//		}
//		if (buffer != null) OS.CPSSetProcessName (psn, buffer);	
		OS.TransformProcessType (psn, OS.kProcessTransformToForegroundApplication);
		OS.SetFrontProcess (psn);
//		ptr = OS.getenv (ascii ("APP_ICON_" + pid));
//		if (ptr != 0) {
//			int image = readImageRef (ptr);
//			if (image != 0) {
//				dockImage = image;
//				OS.SetApplicationDockTileImage (dockImage);
//			}
//		}
	}
	
	pool = (NSAutoreleasePool)new NSAutoreleasePool().alloc().init();
	application = NSApplication.sharedApplication();
}

static void deregister (Display display) {
	synchronized (Device.class) {
		for (int i=0; i<Displays.length; i++) {
			if (display == Displays [i]) Displays [i] = null;
		}
	}
}

/**
 * Destroys the device in the operating system and releases
 * the device's handle.  If the device does not have a handle,
 * this method may do nothing depending on the device.
 * <p>
 * This method is called after <code>release</code>.
 * </p>
 * @see Device#dispose
 * @see #release
 */
protected void destroy () {
	if (this == Default) Default = null;
	deregister (this);
	destroyDisplay ();
}

void destroyDisplay () {
	if (pool != null) pool.release();
	pool = null;
	application = null;
}

/**
 * Causes the <code>run()</code> method of the runnable to
 * be invoked by the user-interface thread just before the
 * receiver is disposed.  Specifying a <code>null</code> runnable
 * is ignored.
 *
 * @param runnable code to run at dispose time.
 * 
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public void disposeExec (Runnable runnable) {
	checkDevice ();
	if (disposeList == null) disposeList = new Runnable [4];
	for (int i=0; i<disposeList.length; i++) {
		if (disposeList [i] == null) {
			disposeList [i] = runnable;
			return;
		}
	}
	Runnable [] newDisposeList = new Runnable [disposeList.length + 4];
	System.arraycopy (disposeList, 0, newDisposeList, 0, disposeList.length);
	newDisposeList [disposeList.length] = runnable;
	disposeList = newDisposeList;
}

void error (int code) {
	SWT.error(code);
}

boolean filterEvent (Event event) {
	if (filterTable != null) filterTable.sendEvent (event);
	return false;
}

boolean filters (int eventType) {
	if (filterTable == null) return false;
	return filterTable.hooks (eventType);
}

/**
 * Given the operating system handle for a widget, returns
 * the instance of the <code>Widget</code> subclass which
 * represents it in the currently running application, if
 * such exists, or null if no matching widget can be found.
 * <p>
 * <b>IMPORTANT:</b> This method should not be called from
 * application code. The arguments are platform-specific.
 * </p>
 *
 * @param handle the handle for the widget
 * @return the SWT widget that the handle represents
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public Widget findWidget (int handle) {
	checkDevice ();
//	return getWidget (handle);
	return null;
}

/**
 * Given the operating system handle for a widget,
 * and widget-specific id, returns the instance of
 * the <code>Widget</code> subclass which represents
 * the handle/id pair in the currently running application,
 * if such exists, or null if no matching widget can be found.
 * <p>
 * <b>IMPORTANT:</b> This method should not be called from
 * application code. The arguments are platform-specific.
 * </p>
 *
 * @param handle the handle for the widget
 * @param id the id for the subwidget (usually an item)
 * @return the SWT widget that the handle/id pair represents
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 * 
 * @since 3.1
 */
public Widget findWidget (int handle, int id) {
	checkDevice ();
	return null;
}

/**
 * Given a widget and a widget-specific id, returns the
 * instance of the <code>Widget</code> subclass which represents
 * the widget/id pair in the currently running application,
 * if such exists, or null if no matching widget can be found.
 *
 * @param widget the widget
 * @param id the id for the subwidget (usually an item)
 * @return the SWT subwidget (usually an item) that the widget/id pair represents
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 * 
 * @since 3.3
 */
public Widget findWidget (Widget widget, int id) {
	checkDevice ();
	return null;
}

/**
 * Returns the display which the given thread is the
 * user-interface thread for, or null if the given thread
 * is not a user-interface thread for any display.  Specifying
 * <code>null</code> as the thread will return <code>null</code>
 * for the display. 
 *
 * @param thread the user-interface thread
 * @return the display for the given thread
 */
public static Display findDisplay (Thread thread) {
	synchronized (Device.class) {
		for (int i=0; i<Displays.length; i++) {
			Display display = Displays [i];
			if (display != null && display.thread == thread) {
				return display;
			}
		}
		return null;
	}
}

/**
 * Returns the currently active <code>Shell</code>, or null
 * if no shell belonging to the currently running application
 * is active.
 *
 * @return the active shell or null
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public Shell getActiveShell () {
	checkDevice ();
//	if (activeShell != null && !activeShell.isDisposed ()) {
//		return activeShell;
//	}
//	for (int i=0; i<widgetTable.length; i++) {
//		Widget widget = widgetTable [i];
//		if (widget != null && widget instanceof Shell) {
//			Shell shell = (Shell) widget;
//			if (OS.IsWindowActive (shell.shellHandle)) return shell;
//		}
//	}
	return null;
}

/**
 * Returns a rectangle describing the receiver's size and location.
 *
 * @return the bounding rectangle
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public Rectangle getBounds () {
	checkDevice ();
	return super.getBounds ();
}

/**
 * Returns the display which the currently running thread is
 * the user-interface thread for, or null if the currently
 * running thread is not a user-interface thread for any display.
 *
 * @return the current display
 */
public static Display getCurrent () {
	return findDisplay (Thread.currentThread ());
}

int getCaretBlinkTime () {
//	checkDevice ();
//	return OS.GetCaretTime () * 1000 / 60;
	return 0;
}

/**
 * Returns a rectangle which describes the area of the
 * receiver which is capable of displaying data.
 * 
 * @return the client area
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 *
 * @see #getBounds
 */
public Rectangle getClientArea () {
	checkDevice ();
	return super.getClientArea ();
}

/**
 * Returns the control which the on-screen pointer is currently
 * over top of, or null if it is not currently over one of the
 * controls built by the currently running application.
 *
 * @return the control under the cursor
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public Control getCursorControl () {
	checkDevice();
	return null;
}

/**
 * Returns the location of the on-screen pointer relative
 * to the top left corner of the screen.
 *
 * @return the cursor location
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public Point getCursorLocation () {
	checkDevice ();
	return null;
}

/**
 * Returns an array containing the recommended cursor sizes.
 *
 * @return the array of cursor sizes
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 * 
 * @since 3.0
 */
public Point [] getCursorSizes () {
	checkDevice ();
	return new Point [] {new Point (16, 16)};
}

/**
 * Returns the default display. One is created (making the
 * thread that invokes this method its user-interface thread)
 * if it did not already exist.
 *
 * @return the default display
 */
public static Display getDefault () {
	synchronized (Device.class) {
		if (Default == null) Default = new Display ();
		return Default;
	}
}

/**
 * Returns the application defined property of the receiver
 * with the specified name, or null if it has not been set.
 * <p>
 * Applications may have associated arbitrary objects with the
 * receiver in this fashion. If the objects stored in the
 * properties need to be notified when the display is disposed
 * of, it is the application's responsibility to provide a
 * <code>disposeExec()</code> handler which does so.
 * </p>
 *
 * @param key the name of the property
 * @return the value of the property or null if it has not been set
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the key is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 *
 * @see #setData(String, Object)
 * @see #disposeExec(Runnable)
 */
public Object getData (String key) {
	checkDevice ();
	if (key == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (keys == null) return null;
	for (int i=0; i<keys.length; i++) {
		if (keys [i].equals (key)) return values [i];
	}
	return null;
}

/**
 * Returns the application defined, display specific data
 * associated with the receiver, or null if it has not been
 * set. The <em>display specific data</em> is a single,
 * unnamed field that is stored with every display. 
 * <p>
 * Applications may put arbitrary objects in this field. If
 * the object stored in the display specific data needs to
 * be notified when the display is disposed of, it is the
 * application's responsibility to provide a
 * <code>disposeExec()</code> handler which does so.
 * </p>
 *
 * @return the display specific data
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 *
 * @see #setData(Object)
 * @see #disposeExec(Runnable)
 */
public Object getData () {
	checkDevice ();
	return data;
}

/**
 * Returns the button dismissal alignment, one of <code>LEFT</code> or <code>RIGHT</code>.
 * The button dismissal alignment is the ordering that should be used when positioning the
 * default dismissal button for a dialog.  For example, in a dialog that contains an OK and
 * CANCEL button, on platforms where the button dismissal alignment is <code>LEFT</code>, the
 * button ordering should be OK/CANCEL.  When button dismissal alignment is <code>RIGHT</code>,
 * the button ordering should be CANCEL/OK.
 *
 * @return the button dismissal order
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 * 
 * @since 2.1
 */
public int getDismissalAlignment () {
	checkDevice ();
	return SWT.RIGHT;
}

/**
 * Returns the longest duration, in milliseconds, between
 * two mouse button clicks that will be considered a
 * <em>double click</em> by the underlying operating system.
 *
 * @return the double click time
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public int getDoubleClickTime () {
	checkDevice ();
	return 0;
//	return OS.GetDblTime () * 1000 / 60; 
}

/**
 * Returns the control which currently has keyboard focus,
 * or null if keyboard events are not currently going to
 * any of the controls built by the currently running
 * application.
 *
 * @return the control under the cursor
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public Control getFocusControl () {
	checkDevice ();
	return null;
}


/**
 * Returns true when the high contrast mode is enabled.
 * Otherwise, false is returned.
 * <p>
 * Note: This operation is a hint and is not supported on
 * platforms that do not have this concept.
 * </p>
 *
 * @return the high contrast mode
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 * 
 * @since 3.0
 */
public boolean getHighContrast () {
	checkDevice ();
	return false;
}

/**
 * Returns the maximum allowed depth of icons on this display, in bits per pixel.
 * On some platforms, this may be different than the actual depth of the display.
 *
 * @return the maximum icon depth
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 * 
 * @see Device#getDepth
 */
public int getIconDepth () {
	return getDepth ();
}

/**
 * Returns an array containing the recommended icon sizes.
 *
 * @return the array of icon sizes
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 * 
 * @see Decorations#setImages(Image[])
 * 
 * @since 3.0
 */
public Point [] getIconSizes () {
	checkDevice ();
	return new Point [] { 
		new Point (16, 16), new Point (32, 32), 
		new Point (64, 64), new Point (128, 128)};	
}

int getLastEventTime () {
	/*
	* This code is intentionally commented.  Event time is
	* in seconds and we need an accurate time in milliseconds.
	*/
//	return (int) (OS.GetLastUserEventTime () * 1000.0);
	return (int) System.currentTimeMillis ();
}

//Menu [] getMenus (Decorations shell) {
//	if (menus == null) return new Menu [0];
//	int count = 0;
//	for (int i = 0; i < menus.length; i++) {
//		Menu menu = menus[i];
//		if (menu != null && menu.parent == shell) count++;
//	}
//	int index = 0;
//	Menu[] result = new Menu[count];
//	for (int i = 0; i < menus.length; i++) {
//		Menu menu = menus[i];
//		if (menu != null && menu.parent == shell) {
//			result[index++] = menu;
//		}
//	}
//	return result;
//}
//
//Menu getMenu (int id) {
//	if (menus == null) return null;
//	int index = id - ID_START;
//	if (0 <= index && index < menus.length) return menus [index];
//	return null;
//}
//
//Menu getMenuBar () {
//	return menuBar;
//}

int getMessageCount () {
	return synchronizer.getMessageCount ();
}

/**
 * Returns an array of monitors attached to the device.
 * 
 * @return the array of monitors
 * 
 * @since 3.0
 */
public Monitor [] getMonitors () {
	checkDevice ();
	NSArray screens = NSScreen.screens();
	int count = screens.count();
	Monitor [] monitors = new Monitor [count];
	for (int i=0; i<count; i++) {
		Monitor monitor = new Monitor ();
		NSScreen screen = new NSScreen(screens.objectAtIndex(i));
		NSRect frame = screen.frame();
		monitor.x = (int)frame.x;
		monitor.y = (int)frame.y;
		monitor.width = (int)frame.width;
		monitor.height = (int)frame.height;
		NSRect visibleFrame = screen.visibleFrame();
		monitor.clientX = (int)visibleFrame.x;
		monitor.clientY = (int)visibleFrame.y;
		monitor.clientWidth = (int)visibleFrame.width;
		monitor.clientHeight = (int)visibleFrame.height;
		monitors [i] = monitor;
	}
	return monitors;
}

/**
 * Returns the primary monitor for that device.
 * 
 * @return the primary monitor
 * 
 * @since 3.0
 */
public Monitor getPrimaryMonitor () {
	checkDevice ();
	Monitor monitor = new Monitor ();
	NSScreen screen = NSScreen.mainScreen();
	NSRect frame = screen.frame();
	monitor.x = (int)frame.x;
	monitor.y = (int)frame.y;
	monitor.width = (int)frame.width;
	monitor.height = (int)frame.height;
	NSRect visibleFrame = screen.visibleFrame();
	monitor.clientX = (int)visibleFrame.x;
	monitor.clientY = (int)visibleFrame.y;
	monitor.clientWidth = (int)visibleFrame.width;
	monitor.clientHeight = (int)visibleFrame.height;
	return monitor;
}

/**
 * Returns a (possibly empty) array containing all shells which have
 * not been disposed and have the receiver as their display.
 *
 * @return the receiver's shells
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public Shell [] getShells () {
	checkDevice ();
//	int index = 0;
//	Shell [] result = new Shell [16];
//	for (int i = 0; i < widgetTable.length; i++) {
//		Widget widget = widgetTable [i];
//		if (widget != null && widget instanceof Shell) {
//			int j = 0;
//			while (j < index) {
//				if (result [j] == widget) break;
//				j++;
//			}
//			if (j == index) {
//				if (index == result.length) {
//					Shell [] newResult = new Shell [index + 16];
//					System.arraycopy (result, 0, newResult, 0, index);
//					result = newResult;
//				}
//				result [index++] = (Shell) widget;	
//			}
//		}
//	}
//	if (index == result.length) return result;
//	Shell [] newResult = new Shell [index];
//	System.arraycopy (result, 0, newResult, 0, index);
//	return newResult;
	return new Shell[0];
}

/**
 * Gets the synchronizer used by the display.
 *
 * @return the receiver's synchronizer
 * 
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 * 
 * @since 3.4
 */
public Synchronizer getSynchronizer () {
	checkDevice ();
	return synchronizer;
}

/**
 * Returns the thread that has invoked <code>syncExec</code>
 * or null if no such runnable is currently being invoked by
 * the user-interface thread.
 * <p>
 * Note: If a runnable invoked by asyncExec is currently
 * running, this method will return null.
 * </p>
 *
 * @return the receiver's sync-interface thread
 * 
 * @exception SWTException <ul>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public Thread getSyncThread () {
	synchronized (Device.class) {
		if (isDisposed ()) error (SWT.ERROR_DEVICE_DISPOSED);
		return synchronizer.syncThread;
	}
}

/**
 * Returns the matching standard color for the given
 * constant, which should be one of the color constants
 * specified in class <code>SWT</code>. Any value other
 * than one of the SWT color constants which is passed
 * in will result in the color black. This color should
 * not be free'd because it was allocated by the system,
 * not the application.
 *
 * @param id the color constant
 * @return the matching color
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 *
 * @see SWT
 */
public Color getSystemColor (int id) {
	checkDevice ();
//	RGBColor rgb = new RGBColor ();
//	switch (id) {
//		case SWT.COLOR_INFO_FOREGROUND: return super.getSystemColor (SWT.COLOR_BLACK);
//		case SWT.COLOR_INFO_BACKGROUND: return Color.carbon_new (this, new float [] {0xFF / 255f, 0xFF / 255f, 0xE1 / 255f, 1});
//		case SWT.COLOR_TITLE_FOREGROUND: OS.GetThemeTextColor((short)OS.kThemeTextColorDocumentWindowTitleActive, (short)getDepth(), true, rgb); break;
//		case SWT.COLOR_TITLE_BACKGROUND: OS.GetThemeBrushAsColor((short)-5/*undocumented darker highlight color*/, (short)getDepth(), true, rgb); break;
//		case SWT.COLOR_TITLE_BACKGROUND_GRADIENT: 	OS.GetThemeBrushAsColor((short)OS.kThemeBrushPrimaryHighlightColor, (short)getDepth(), true, rgb) ; break;
//		case SWT.COLOR_TITLE_INACTIVE_FOREGROUND:	OS.GetThemeTextColor((short)OS.kThemeTextColorDocumentWindowTitleInactive, (short)getDepth(), true, rgb); break;
//		case SWT.COLOR_TITLE_INACTIVE_BACKGROUND: 	OS.GetThemeBrushAsColor((short)OS.kThemeBrushSecondaryHighlightColor, (short)getDepth(), true, rgb); break;
//		case SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT: OS.GetThemeBrushAsColor((short)OS.kThemeBrushSecondaryHighlightColor, (short)getDepth(), true, rgb); break;
//		case SWT.COLOR_WIDGET_DARK_SHADOW: return Color.carbon_new (this, new float [] {0x33 / 255f, 0x33 / 255f, 0x33 / 255f, 1});
//		case SWT.COLOR_WIDGET_NORMAL_SHADOW: return Color.carbon_new (this, new float [] {0x66 / 255f, 0x66 / 255f, 0x66 / 255f, 1});
//		case SWT.COLOR_WIDGET_LIGHT_SHADOW: return Color.carbon_new (this, new float [] {0x99 / 255f, 0x99 / 255f, 0x99 / 255f, 1});
//		case SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW: return Color.carbon_new (this, new float [] {0xCC / 255f, 0xCC / 255f, 0xCC / 255f, 1});
//		case SWT.COLOR_WIDGET_BACKGROUND: OS.GetThemeBrushAsColor((short)OS.kThemeBrushButtonFaceActive, (short)getDepth(), true, rgb); break;
//		case SWT.COLOR_WIDGET_FOREGROUND: OS.GetThemeTextColor((short)OS.kThemeTextColorPushButtonActive, (short)getDepth(), true, rgb); break;
//		case SWT.COLOR_WIDGET_BORDER: return super.getSystemColor (SWT.COLOR_BLACK);
//		case SWT.COLOR_LIST_FOREGROUND: OS.GetThemeTextColor((short)OS.kThemeTextColorListView, (short)getDepth(), true, rgb); break;
//		case SWT.COLOR_LIST_BACKGROUND: OS.GetThemeBrushAsColor((short)OS.kThemeBrushListViewBackground, (short)getDepth(), true, rgb); break;
//		case SWT.COLOR_LIST_SELECTION_TEXT: OS.GetThemeTextColor((short)OS.kThemeTextColorListView, (short)getDepth(), true, rgb); break;
//		case SWT.COLOR_LIST_SELECTION: OS.GetThemeBrushAsColor((short)OS.kThemeBrushPrimaryHighlightColor, (short)getDepth(), true, rgb); break;
//		default:
//			return super.getSystemColor (id);	
//	}
//	float red = ((rgb.red >> 8) & 0xFF) / 255f;
//	float green = ((rgb.green >> 8) & 0xFF) / 255f;
//	float blue = ((rgb.blue >> 8) & 0xFF) / 255f;
//	return Color.carbon_new (this, new float[]{red, green, blue, 1});
	return super.getSystemColor(id);
}

/**
 * Returns the matching standard platform cursor for the given
 * constant, which should be one of the cursor constants
 * specified in class <code>SWT</code>. This cursor should
 * not be free'd because it was allocated by the system,
 * not the application.  A value of <code>null</code> will
 * be returned if the supplied constant is not an SWT cursor
 * constant. 
 *
 * @param id the SWT cursor constant
 * @return the corresponding cursor or <code>null</code>
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 *
 * @see SWT#CURSOR_ARROW
 * @see SWT#CURSOR_WAIT
 * @see SWT#CURSOR_CROSS
 * @see SWT#CURSOR_APPSTARTING
 * @see SWT#CURSOR_HELP
 * @see SWT#CURSOR_SIZEALL
 * @see SWT#CURSOR_SIZENESW
 * @see SWT#CURSOR_SIZENS
 * @see SWT#CURSOR_SIZENWSE
 * @see SWT#CURSOR_SIZEWE
 * @see SWT#CURSOR_SIZEN
 * @see SWT#CURSOR_SIZES
 * @see SWT#CURSOR_SIZEE
 * @see SWT#CURSOR_SIZEW
 * @see SWT#CURSOR_SIZENE
 * @see SWT#CURSOR_SIZESE
 * @see SWT#CURSOR_SIZESW
 * @see SWT#CURSOR_SIZENW
 * @see SWT#CURSOR_UPARROW
 * @see SWT#CURSOR_IBEAM
 * @see SWT#CURSOR_NO
 * @see SWT#CURSOR_HAND
 * 
 * @since 3.0
 */
public Cursor getSystemCursor (int id) {
	checkDevice ();
	if (!(0 <= id && id < cursors.length)) return null;
	if (cursors [id] == null) {
		cursors [id] = new Cursor (this, id);
	}
	return cursors [id];
}

/**
 * Returns the matching standard platform image for the given
 * constant, which should be one of the icon constants
 * specified in class <code>SWT</code>. This image should
 * not be free'd because it was allocated by the system,
 * not the application.  A value of <code>null</code> will
 * be returned either if the supplied constant is not an
 * SWT icon constant or if the platform does not define an
 * image that corresponds to the constant. 
 *
 * @param id the SWT icon constant
 * @return the corresponding image or <code>null</code>
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 *
 * @see SWT#ICON_ERROR
 * @see SWT#ICON_INFORMATION
 * @see SWT#ICON_QUESTION
 * @see SWT#ICON_WARNING
 * @see SWT#ICON_WORKING
 * 
 * @since 3.0
 */
public Image getSystemImage (int id) {
	checkDevice ();
//	switch (id) {
//		case SWT.ICON_ERROR: {	
//			if (errorImage != null) return errorImage;
//			int [] image = createImage (OS.kAlertStopIcon);
//			if (image == null) break;
//			return errorImage = Image.carbon_new (this, SWT.ICON, image [0], image [1]);
//		}
//		case SWT.ICON_INFORMATION:
//		case SWT.ICON_QUESTION:
//		case SWT.ICON_WORKING: {
//			if (infoImage != null) return infoImage;
//			int [] image = createImage (OS.kAlertNoteIcon);
//			if (image == null) break;
//			return infoImage = Image.carbon_new (this, SWT.ICON, image [0], image [1]);
//		}
//		case SWT.ICON_WARNING: {
//			if (warningImage != null) return warningImage;
//			int [] image = createImage (OS.kAlertCautionIcon);
//			if (image == null) break;
//			return warningImage = Image.carbon_new (this, SWT.ICON, image [0], image [1]);
//		}
//	}
	
	//[[NSWorkspace sharedWorkspace] iconForFileType:NSFileTypeForHFSTypeCode(code)];
	return null;
//	return Image.cocoa_new (this, SWT.ICON, NSImage.imageNamed (NSString.stringWith("NSImageNameGoRightTemplate")));
}

/**
 * Returns the single instance of the system tray or null
 * when there is no system tray available for the platform.
 *
 * @return the system tray or <code>null</code>
 * 
 * @exception SWTException <ul>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 *
 * @since 3.0
 */
public Tray getSystemTray () {
	checkDevice ();
	if (tray != null) return tray;
	return tray = new Tray (this, SWT.NONE);
}

/**
 * Returns the user-interface thread for the receiver.
 *
 * @return the receiver's user-interface thread
 * 
 * @exception SWTException <ul>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 */
public Thread getThread () {
	synchronized (Device.class) {
		if (isDisposed ()) error (SWT.ERROR_DEVICE_DISPOSED);
		return thread;
	}
}

/**
 * Initializes any internal resources needed by the
 * device.
 * <p>
 * This method is called after <code>create</code>.
 * </p>
 * 
 * @see #create
 */
protected void init () {
	super.init ();
	initClasses ();
}

void initClasses () {
	dialogCallback3 = new Callback(this, "dialogProc", 3);
	int dialogProc3 = dialogCallback3.getAddress();
	if (dialogProc3 == 0) error (SWT.ERROR_NO_MORE_CALLBACKS);
	
	windowDelegateCallback3 = new Callback(this, "windowDelegateProc", 3);
	int proc3 = windowDelegateCallback3.getAddress();
	if (proc3 == 0) error (SWT.ERROR_NO_MORE_CALLBACKS);
	windowDelegateCallback2 = new Callback(this, "windowDelegateProc", 2);
	int proc2 = windowDelegateCallback2.getAddress();
	if (proc2 == 0) error (SWT.ERROR_NO_MORE_CALLBACKS);
	windowDelegateCallback4 = new Callback(this, "windowDelegateProc", 4);
	int proc4 = windowDelegateCallback4.getAddress();
	if (proc4 == 0) error (SWT.ERROR_NO_MORE_CALLBACKS);
	windowDelegateCallback5 = new Callback(this, "windowDelegateProc", 5);
	int proc5 = windowDelegateCallback5.getAddress();
	if (proc5 == 0) error (SWT.ERROR_NO_MORE_CALLBACKS);

	String className = "SWTWindowDelegate";
	int cls = OS.objc_allocateClassPair(OS.class_NSObject, className, 0);
	OS.class_addIvar(cls, "tag", OS.PTR_SIZEOF, (byte)(Math.log(OS.PTR_SIZEOF) / Math.log(2)), "i");
	OS.class_addMethod(cls, OS.sel_windowDidResize_1, proc3, "@:@");
	OS.class_addMethod(cls, OS.sel_windowShouldClose_1, proc3, "@:@");
	OS.class_addMethod(cls, OS.sel_windowWillClose_1, proc3, "@:@");
	OS.class_addMethod(cls, OS.sel_tag, proc2, "@:");
	OS.class_addMethod(cls, OS.sel_setTag_1, proc3, "@:i");
	OS.objc_registerClassPair(cls);
	
	className = "SWTPanelDelegate";
	cls = OS.objc_allocateClassPair(OS.class_NSObject, className, 0);
	OS.class_addIvar(cls, "tag", OS.PTR_SIZEOF, (byte)(Math.log(OS.PTR_SIZEOF) / Math.log(2)), "i");
	OS.class_addMethod(cls, OS.sel_windowWillClose_1, dialogProc3, "@:@");
	OS.class_addMethod(cls, OS.sel_changeColor_1, dialogProc3, "@:@");
	OS.class_addMethod(cls, OS.sel_changeFont_1, dialogProc3, "@:@");
	OS.class_addMethod(cls, OS.sel_tag, proc2, "@:");
	OS.class_addMethod(cls, OS.sel_setTag_1, proc3, "@:i");
	OS.class_addMethod(cls, OS.sel_menuForEvent_1, proc3, "@:@");
	OS.objc_registerClassPair(cls);
	
	className = "SWTView";
	cls = OS.objc_allocateClassPair(OS.class_NSScrollView, className, 0);
	OS.class_addIvar(cls, "tag", OS.PTR_SIZEOF, (byte)(Math.log(OS.PTR_SIZEOF) / Math.log(2)), "i");
	OS.class_addMethod(cls, OS.sel_tag, proc2, "@:");
	OS.class_addMethod(cls, OS.sel_setTag_1, proc3, "@:i");
	OS.class_addMethod(cls, OS.sel_isFlipped, proc2, "@:");
	OS.class_addMethod(cls, OS.sel_drawRect_1, OS.drawRect_CALLBACK(proc3), "@:i");
//	OS.class_addMethod(cls, OS.sel_mouseDown_1, proc3, "@:@");
//	OS.class_addMethod(cls, OS.sel_keyDown_1, proc3, "@:@");
	OS.class_addMethod(cls, OS.sel_menuForEvent_1, proc3, "@:@");
	OS.objc_registerClassPair(cls);
	
	className = "SWTButton";
	cls = OS.objc_allocateClassPair(OS.class_NSButton, className, 0);
//	OS.class_addMethod(cls, OS.sel_isFlipped, proc2, "@:");
//	OS.class_addMethod(cls, OS.sel_mouseDown_1, proc3, "@:@");
//	OS.class_addMethod(cls, OS.sel_keyDown_1, proc3, "@:@");
//	OS.class_addMethod(cls, OS.sel_drawRect_1, proc3, "@:@");
	OS.class_addMethod(cls, OS.sel_sendSelection, proc2, "@:");
	OS.class_addMethod(cls, OS.sel_menuForEvent_1, proc3, "@:@");
	OS.objc_registerClassPair(cls);
	
	className = "SWTTableView";
	cls = OS.objc_allocateClassPair(OS.class_NSTableView, className, 0);
	OS.class_addMethod(cls, OS.sel_sendDoubleSelection, proc2, "@:");
//	OS.class_addMethod(cls, OS.sel_isFlipped, proc2, "@:");
	OS.class_addMethod(cls, OS.sel_numberOfRowsInTableView_1, proc3, "@:@");
	OS.class_addMethod(cls, OS.sel_tableView_1objectValueForTableColumn_1row_1, proc5, "@:@:@:@");
	OS.class_addMethod(cls, OS.sel_tableView_1shouldEditTableColumn_1row_1, proc5, "@:@:@:@");
	OS.class_addMethod(cls, OS.sel_tableViewSelectionDidChange_1, proc3, "@:@");
	OS.class_addMethod(cls, OS.sel_menuForEvent_1, proc3, "@:@");
	OS.objc_registerClassPair(cls);
	
	className = "SWTTabView";
	cls = OS.objc_allocateClassPair(OS.class_NSTabView, className, 0);
//	OS.class_addMethod(cls, OS.sel_isFlipped, proc2, "@:");
	OS.class_addIvar(cls, "tag", OS.PTR_SIZEOF, (byte)(Math.log(OS.PTR_SIZEOF) / Math.log(2)), "i");
	OS.class_addMethod(cls, OS.sel_tabView_1willSelectTabViewItem_1, proc4, "@:@@");
	OS.class_addMethod(cls, OS.sel_tag, proc2, "@:");
	OS.class_addMethod(cls, OS.sel_setTag_1, proc3, "@:i");
	OS.class_addMethod(cls, OS.sel_menuForEvent_1, proc3, "@:@");
	OS.objc_registerClassPair(cls);
	
	className = "SWTBox";
	cls = OS.objc_allocateClassPair(OS.class_NSBox, className, 0);
//	OS.class_addMethod(cls, OS.sel_isFlipped, proc2, "@:");
	OS.class_addIvar(cls, "tag", OS.PTR_SIZEOF, (byte)(Math.log(OS.PTR_SIZEOF) / Math.log(2)), "i");
	OS.class_addMethod(cls, OS.sel_tag, proc2, "@:");
	OS.class_addMethod(cls, OS.sel_setTag_1, proc3, "@:i");
	OS.class_addMethod(cls, OS.sel_menuForEvent_1, proc3, "@:@");
	OS.objc_registerClassPair(cls);
	
	className = "SWTProgressIndicator";
	cls = OS.objc_allocateClassPair(OS.class_NSProgressIndicator, className, 0);
//	OS.class_addMethod(cls, OS.sel_isFlipped, proc2, "@:");
	OS.class_addIvar(cls, "tag", OS.PTR_SIZEOF, (byte)(Math.log(OS.PTR_SIZEOF) / Math.log(2)), "i");
	OS.class_addMethod(cls, OS.sel_tag, proc2, "@:");
	OS.class_addMethod(cls, OS.sel_setTag_1, proc3, "@:i");
	OS.class_addMethod(cls, OS.sel_menuForEvent_1, proc3, "@:@");
	OS.objc_registerClassPair(cls); 

	className = "SWTSlider";
	cls = OS.objc_allocateClassPair(OS.class_NSSlider, className, 0);
//	OS.class_addMethod(cls, OS.sel_isFlipped, proc2, "@:");
	OS.class_addMethod(cls, OS.sel_menuForEvent_1, proc3, "@:@");
	OS.objc_registerClassPair(cls); 
	
	className = "SWTPopUpButton";
	cls = OS.objc_allocateClassPair(OS.class_NSPopUpButton, className, 0);
//	OS.class_addMethod(cls, OS.sel_isFlipped, proc2, "@:");
	OS.class_addMethod(cls, OS.sel_sendSelection, proc2, "@:");
	OS.class_addMethod(cls, OS.sel_menuForEvent_1, proc3, "@:@");
	OS.objc_registerClassPair(cls);
	
	className = "SWTComboBox";
	cls = OS.objc_allocateClassPair(OS.class_NSComboBox, className, 0);
//	OS.class_addMethod(cls, OS.sel_isFlipped, proc2, "@:");
	OS.class_addMethod(cls, OS.sel_comboBoxSelectionDidChange_1, proc3, "@:@");
	OS.class_addMethod(cls, OS.sel_sendSelection, proc2, "@:");
	OS.class_addMethod(cls, OS.sel_menuForEvent_1, proc3, "@:@");
	OS.objc_registerClassPair(cls);
	
	className = "SWTDatePicker";
	cls = OS.objc_allocateClassPair(OS.class_NSDatePicker, className, 0);
//	OS.class_addMethod(cls, OS.sel_isFlipped, proc2, "@:");
	OS.class_addMethod(cls, OS.sel_sendSelection, proc2, "@:");
	OS.class_addMethod(cls, OS.sel_menuForEvent_1, proc3, "@:@");
	OS.objc_registerClassPair(cls);

	className = "SWTImageView";
	cls = OS.objc_allocateClassPair(OS.class_NSImageView, className, 0);
//	OS.class_addMethod(cls, OS.sel_isFlipped, proc2, "@:");
	OS.class_addMethod(cls, OS.sel_drawRect_1, OS.drawRect_CALLBACK(proc3), "@:i");
	OS.class_addMethod(cls, OS.sel_mouseDown_1, proc3, "@:@");
	OS.class_addMethod(cls, OS.sel_mouseUp_1, proc3, "@:@");
	OS.class_addMethod(cls, OS.sel_rightMouseDown_1, proc3, "@:@");
	OS.class_addMethod(cls, OS.sel_menuForEvent_1, proc3, "@:@");
	OS.objc_registerClassPair(cls);

	className = "SWTStepper";
	cls = OS.objc_allocateClassPair(OS.class_NSStepper, className, 0);
//	OS.class_addMethod(cls, OS.sel_isFlipped, proc2, "@:");
	OS.class_addMethod(cls, OS.sel_sendSelection, proc2, "@:");
	OS.class_addMethod(cls, OS.sel_menuForEvent_1, proc3, "@:@");
	OS.objc_registerClassPair(cls);

	className = "SWTTextView";
	cls = OS.objc_allocateClassPair(OS.class_NSTextView, className, 0);
//	OS.class_addMethod(cls, OS.sel_isFlipped, proc2, "@:");
//	OS.class_addMethod(cls, OS.sel_sendSelection, proc2, "@:");
	OS.class_addIvar(cls, "tag", OS.PTR_SIZEOF, (byte)(Math.log(OS.PTR_SIZEOF) / Math.log(2)), "i");
	OS.class_addMethod(cls, OS.sel_tag, proc2, "@:");
	OS.class_addMethod(cls, OS.sel_setTag_1, proc3, "@:i");
	OS.class_addMethod(cls, OS.sel_menuForEvent_1, proc3, "@:@");
	OS.objc_registerClassPair(cls);
}

/**	 
 * Invokes platform specific functionality to allocate a new GC handle.
 * <p>
 * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
 * API for <code>Display</code>. It is marked public only so that it
 * can be shared within the packages provided by SWT. It is not
 * available on all platforms, and should never be called from
 * application code.
 * </p>
 *
 * @param data the platform specific GC data 
 * @return the platform specific GC handle
 * 
 * @exception SWTException <ul>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 * @exception SWTError <ul>
 *    <li>ERROR_NO_HANDLES if a handle could not be obtained for gc creation</li>
 * </ul>
 */
public int internal_new_GC (GCData data) {
	if (isDisposed()) SWT.error(SWT.ERROR_DEVICE_DISPOSED);
	//TODO - multiple monitors
	return 0;
}

/**	 
 * Invokes platform specific functionality to dispose a GC handle.
 * <p>
 * <b>IMPORTANT:</b> This method is <em>not</em> part of the public
 * API for <code>Display</code>. It is marked public only so that it
 * can be shared within the packages provided by SWT. It is not
 * available on all platforms, and should never be called from
 * application code.
 * </p>
 *
 * @param hDC the platform specific GC handle
 * @param data the platform specific GC data 
 */
public void internal_dispose_GC (int context, GCData data) {
	if (isDisposed()) SWT.error(SWT.ERROR_DEVICE_DISPOSED);
	
}

static boolean isValidClass (Class clazz) {
	String name = clazz.getName ();
	int index = name.lastIndexOf ('.');
	return name.substring (0, index + 1).equals (PACKAGE_PREFIX);
}

boolean isValidThread () {
	return thread == Thread.currentThread ();
}

/**
 * Generate a low level system event.
 * 
 * <code>post</code> is used to generate low level keyboard
 * and mouse events. The intent is to enable automated UI
 * testing by simulating the input from the user.  Most
 * SWT applications should never need to call this method.
 * <p>
 * Note that this operation can fail when the operating system
 * fails to generate the event for any reason.  For example,
 * this can happen when there is no such key or mouse button
 * or when the system event queue is full.
 * </p>
 * <p>
 * <b>Event Types:</b>
 * <p>KeyDown, KeyUp
 * <p>The following fields in the <code>Event</code> apply:
 * <ul>
 * <li>(in) type KeyDown or KeyUp</li>
 * <p> Either one of:
 * <li>(in) character a character that corresponds to a keyboard key</li>
 * <li>(in) keyCode the key code of the key that was typed,
 *          as defined by the key code constants in class <code>SWT</code></li>
 * </ul>
 * <p>MouseDown, MouseUp</p>
 * <p>The following fields in the <code>Event</code> apply:
 * <ul>
 * <li>(in) type MouseDown or MouseUp
 * <li>(in) button the button that is pressed or released
 * </ul>
 * <p>MouseMove</p>
 * <p>The following fields in the <code>Event</code> apply:
 * <ul>
 * <li>(in) type MouseMove
 * <li>(in) x the x coordinate to move the mouse pointer to in screen coordinates
 * <li>(in) y the y coordinate to move the mouse pointer to in screen coordinates
 * </ul>
 * </dl>
 * 
 * @param event the event to be generated
 * 
 * @return true if the event was generated or false otherwise
 * 
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the event is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 *
 * @since 3.0
 * 
 */
public boolean post(Event event) {
	synchronized (Device.class) {
		if (isDisposed ()) error (SWT.ERROR_DEVICE_DISPOSED);
		if (event == null) error (SWT.ERROR_NULL_ARGUMENT);
//		int type = event.type;
//		switch (type) {
//			case SWT.KeyDown:
//			case SWT.KeyUp: {
//				int vKey = Display.untranslateKey (event.keyCode);
//				if (vKey != 0) {
//					return OS.CGPostKeyboardEvent (0, vKey, type == SWT.KeyDown) == 0;
//				} else {
//					vKey = -1;
//					int kchrPtr = OS.GetScriptManagerVariable ((short) OS.smKCHRCache);
//					int key = -1;
//					int [] state = new int [1];
//					int [] encoding = new int [1];
//					short keyScript = (short) OS.GetScriptManagerVariable ((short) OS.smKeyScript);
//					short regionCode = (short) OS.GetScriptManagerVariable ((short) OS.smRegionCode);
//					if (OS.UpgradeScriptInfoToTextEncoding (keyScript, (short) OS.kTextLanguageDontCare, regionCode, null, encoding) == OS.paramErr) {
//						if (OS.UpgradeScriptInfoToTextEncoding (keyScript, (short) OS.kTextLanguageDontCare, (short) OS.kTextRegionDontCare, null, encoding) == OS.paramErr) {
//							encoding [0] = OS.kTextEncodingMacRoman;
//						}
//					}
//					int [] encodingInfo = new int [1];
//					OS.CreateUnicodeToTextInfoByEncoding (encoding [0], encodingInfo);
//					if (encodingInfo [0] != 0) {
//						char [] input = {event.character};
//						byte [] buffer = new byte [2];
//						OS.ConvertFromUnicodeToPString (encodingInfo [0], 2, input, buffer);
//						OS.DisposeUnicodeToTextInfo (encodingInfo);
//						key = buffer [1] & 0x7f;
//					}
//					if (key == -1) return false;				
//					for (int i = 0 ; i <= 0x7F ; i++) {
//						int result1 = OS.KeyTranslate (kchrPtr, (short) (i | 512), state);
//						int result2 = OS.KeyTranslate (kchrPtr, (short) i, state);
//						if ((result1 & 0x7f) == key || (result2 & 0x7f) == key) {
//							vKey = i;
//							break;
//						}
//					}
//					if (vKey == -1) return false;
//					return OS.CGPostKeyboardEvent (key, vKey, type == SWT.KeyDown) == 0;
//				}
//			}
//			case SWT.MouseDown:
//			case SWT.MouseMove: 
//			case SWT.MouseUp: {
//				CGPoint mouseCursorPosition = new CGPoint ();
//				int chord = OS.GetCurrentEventButtonState ();
//				if (type == SWT.MouseMove) {
//					mouseCursorPosition.x = event.x;
//					mouseCursorPosition.y = event.y;
//					return OS.CGPostMouseEvent (mouseCursorPosition, true, 5, (chord & 0x1) != 0, (chord & 0x2) != 0, (chord & 0x4) != 0, (chord & 0x8) != 0, (chord & 0x10) != 0) == 0;
//				} else {
//					int button = event.button;
//					if (button < 1 || button > 5) return false;
//					boolean button1 = false, button2 = false, button3 = false, button4 = false, button5 = false;
//	 				switch (button) {
//						case 1: {
//							button1 = type == SWT.MouseDown;
//							button2 = (chord & 0x4) != 0;
//							button3 = (chord & 0x2) != 0;
//							button4 = (chord & 0x8) != 0;
//							button5 = (chord & 0x10) != 0;
//							break;
//						}
//						case 2: {
//							button1 = (chord & 0x1) != 0;
//							button2 = type == SWT.MouseDown;
//							button3 = (chord & 0x2) != 0;
//							button4 = (chord & 0x8) != 0;
//							button5 = (chord & 0x10) != 0;
//							break;
//						}
//						case 3: {
//							button1 = (chord & 0x1) != 0;
//							button2 = (chord & 0x4) != 0;
//							button3 = type == SWT.MouseDown;
//							button4 = (chord & 0x8) != 0;
//							button5 = (chord & 0x10) != 0;
//							break;
//						}
//						case 4: {
//							button1 = (chord & 0x1) != 0;
//							button2 = (chord & 0x4) != 0;
//							button3 = (chord & 0x2) != 0;
//							button4 = type == SWT.MouseDown;
//							button5 = (chord & 0x10) != 0;
//							break;
//						}
//						case 5: {
//							button1 = (chord & 0x1) != 0;
//							button2 = (chord & 0x4) != 0;
//							button3 = (chord & 0x2) != 0;
//							button4 = (chord & 0x8) != 0;
//							button5 = type == SWT.MouseDown;
//							break;
//						}
//					}
//					org.eclipse.swt.internal.carbon.Point pt = new org.eclipse.swt.internal.carbon.Point ();
//					OS.GetGlobalMouse (pt);
//					mouseCursorPosition.x = pt.h;
//					mouseCursorPosition.y = pt.v;
//					return OS.CGPostMouseEvent (mouseCursorPosition, true, 5, button1, button3, button2, button4, button5) == 0;
//				}
//			}
//			case SWT.MouseWheel: {
//				return OS.CGPostScrollWheelEvent (1, event.count) == 0;
//			}
//		} 
		return false;
	}
}

void postEvent (Event event) {
	/*
	* Place the event at the end of the event queue.
	* This code is always called in the Display's
	* thread so it must be re-enterant but does not
	* need to be synchronized.
	*/
	if (eventQueue == null) eventQueue = new Event [4];
	int index = 0;
	int length = eventQueue.length;
	while (index < length) {
		if (eventQueue [index] == null) break;
		index++;
	}
	if (index == length) {
		Event [] newQueue = new Event [length + 4];
		System.arraycopy (eventQueue, 0, newQueue, 0, length);
		eventQueue = newQueue;
	}
	eventQueue [index] = event;
}

/**
 * Maps a point from one coordinate system to another.
 * When the control is null, coordinates are mapped to
 * the display.
 * <p>
 * NOTE: On right-to-left platforms where the coordinate
 * systems are mirrored, special care needs to be taken
 * when mapping coordinates from one control to another
 * to ensure the result is correctly mirrored.
 * 
 * Mapping a point that is the origin of a rectangle and
 * then adding the width and height is not equivalent to
 * mapping the rectangle.  When one control is mirrored
 * and the other is not, adding the width and height to a
 * point that was mapped causes the rectangle to extend
 * in the wrong direction.  Mapping the entire rectangle
 * instead of just one point causes both the origin and
 * the corner of the rectangle to be mapped.
 * </p>
 * 
 * @param from the source <code>Control</code> or <code>null</code>
 * @param to the destination <code>Control</code> or <code>null</code>
 * @param point to be mapped 
 * @return point with mapped coordinates 
 * 
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the point is null</li>
 *    <li>ERROR_INVALID_ARGUMENT - if the Control from or the Control to have been disposed</li> 
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 * 
 * @since 2.1.2
 */
public Point map (Control from, Control to, Point point) {
	checkDevice ();
	if (point == null) error (SWT.ERROR_NULL_ARGUMENT);	
	return map (from, to, point.x, point.y);
}

/**
 * Maps a point from one coordinate system to another.
 * When the control is null, coordinates are mapped to
 * the display.
 * <p>
 * NOTE: On right-to-left platforms where the coordinate
 * systems are mirrored, special care needs to be taken
 * when mapping coordinates from one control to another
 * to ensure the result is correctly mirrored.
 * 
 * Mapping a point that is the origin of a rectangle and
 * then adding the width and height is not equivalent to
 * mapping the rectangle.  When one control is mirrored
 * and the other is not, adding the width and height to a
 * point that was mapped causes the rectangle to extend
 * in the wrong direction.  Mapping the entire rectangle
 * instead of just one point causes both the origin and
 * the corner of the rectangle to be mapped.
 * </p>
 * 
 * @param from the source <code>Control</code> or <code>null</code>
 * @param to the destination <code>Control</code> or <code>null</code>
 * @param x coordinates to be mapped
 * @param y coordinates to be mapped
 * @return point with mapped coordinates
 * 
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_INVALID_ARGUMENT - if the Control from or the Control to have been disposed</li> 
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 * 
 * @since 2.1.2
 */
public Point map (Control from, Control to, int x, int y) {
	checkDevice ();
	if (from != null && from.isDisposed()) error (SWT.ERROR_INVALID_ARGUMENT);
	if (to != null && to.isDisposed()) error (SWT.ERROR_INVALID_ARGUMENT);
	Point point = new Point (x, y);
//	if (from == to) return point;
//	Rect rect = new Rect ();
//	if (from != null) {
//		int window = OS.GetControlOwner (from.handle);
//		CGPoint pt = new CGPoint ();
//		OS.HIViewConvertPoint (pt, from.handle, 0);
//		point.x += (int) pt.x;
//		point.y += (int) pt.y;
//		OS.GetWindowBounds (window, (short) OS.kWindowStructureRgn, rect);
//		point.x += rect.left;
//		point.y += rect.top;
//		Rect inset = from.getInset ();
//		point.x -= inset.left; 
//		point.y -= inset.top;
//	}
//	if (to != null) {
//		int window = OS.GetControlOwner (to.handle);
//		CGPoint pt = new CGPoint ();
//		OS.HIViewConvertPoint (pt, to.handle, 0);
//		point.x -= (int) pt.x;
//		point.y -= (int) pt.y;
//		OS.GetWindowBounds (window, (short) OS.kWindowStructureRgn, rect);
//		point.x -= rect.left;
//		point.y -= rect.top;
//		Rect inset = to.getInset ();
//		point.x += inset.left; 
//		point.y += inset.top;
//	}
	return point;
}

/**
 * Maps a point from one coordinate system to another.
 * When the control is null, coordinates are mapped to
 * the display.
 * <p>
 * NOTE: On right-to-left platforms where the coordinate
 * systems are mirrored, special care needs to be taken
 * when mapping coordinates from one control to another
 * to ensure the result is correctly mirrored.
 * 
 * Mapping a point that is the origin of a rectangle and
 * then adding the width and height is not equivalent to
 * mapping the rectangle.  When one control is mirrored
 * and the other is not, adding the width and height to a
 * point that was mapped causes the rectangle to extend
 * in the wrong direction.  Mapping the entire rectangle
 * instead of just one point causes both the origin and
 * the corner of the rectangle to be mapped.
 * </p>
 * 
 * @param from the source <code>Control</code> or <code>null</code>
 * @param to the destination <code>Control</code> or <code>null</code>
 * @param rectangle to be mapped
 * @return rectangle with mapped coordinates
 * 
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the rectangle is null</li>
 *    <li>ERROR_INVALID_ARGUMENT - if the Control from or the Control to have been disposed</li> 
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 * 
 * @since 2.1.2
 */
public Rectangle map (Control from, Control to, Rectangle rectangle) {
	checkDevice ();
	if (rectangle == null) error (SWT.ERROR_NULL_ARGUMENT);	
	return map (from, to, rectangle.x, rectangle.y, rectangle.width, rectangle.height);
}

/**
 * Maps a point from one coordinate system to another.
 * When the control is null, coordinates are mapped to
 * the display.
 * <p>
 * NOTE: On right-to-left platforms where the coordinate
 * systems are mirrored, special care needs to be taken
 * when mapping coordinates from one control to another
 * to ensure the result is correctly mirrored.
 * 
 * Mapping a point that is the origin of a rectangle and
 * then adding the width and height is not equivalent to
 * mapping the rectangle.  When one control is mirrored
 * and the other is not, adding the width and height to a
 * point that was mapped causes the rectangle to extend
 * in the wrong direction.  Mapping the entire rectangle
 * instead of just one point causes both the origin and
 * the corner of the rectangle to be mapped.
 * </p>
 * 
 * @param from the source <code>Control</code> or <code>null</code>
 * @param to the destination <code>Control</code> or <code>null</code>
 * @param x coordinates to be mapped
 * @param y coordinates to be mapped
 * @param width coordinates to be mapped
 * @param height coordinates to be mapped
 * @return rectangle with mapped coordinates
 * 
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_INVALID_ARGUMENT - if the Control from or the Control to have been disposed</li> 
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 * 
 * @since 2.1.2
 */
public Rectangle map (Control from, Control to, int x, int y, int width, int height) {
	checkDevice ();
	if (from != null && from.isDisposed()) error (SWT.ERROR_INVALID_ARGUMENT);
	if (to != null && to.isDisposed()) error (SWT.ERROR_INVALID_ARGUMENT);
	Rectangle rectangle = new Rectangle (x, y, width, height);
//	if (from == to) return rectangle;
//	Rect rect = new Rect ();
//	if (from != null) {
//		int window = OS.GetControlOwner (from.handle);
//		CGPoint pt = new CGPoint ();
//		OS.HIViewConvertPoint (pt, from.handle, 0);
//		rectangle.x += (int) pt.x;
//		rectangle.y += (int) pt.y;
//		OS.GetWindowBounds (window, (short) OS.kWindowStructureRgn, rect);
//		rectangle.x += rect.left;
//		rectangle.y += rect.top;
//		Rect inset = from.getInset ();
//		rectangle.x -= inset.left; 
//		rectangle.y -= inset.top;
//	}
//	if (to != null) {
//		int window = OS.GetControlOwner (to.handle);
//		CGPoint pt = new CGPoint ();
//		OS.HIViewConvertPoint (pt, to.handle, 0);
//		rectangle.x -= (int) pt.x;
//		rectangle.y -= (int) pt.y;
//		OS.GetWindowBounds (window, (short) OS.kWindowStructureRgn, rect);
//		rectangle.x -= rect.left;
//		rectangle.y -= rect.top;
//		Rect inset = to.getInset ();
//		rectangle.x += inset.left; 
//		rectangle.y += inset.top;
//	}
	return rectangle;
}

/**
 * Reads an event from the operating system's event queue,
 * dispatches it appropriately, and returns <code>true</code>
 * if there is potentially more work to do, or <code>false</code>
 * if the caller can sleep until another event is placed on
 * the event queue.
 * <p>
 * In addition to checking the system event queue, this method also
 * checks if any inter-thread messages (created by <code>syncExec()</code>
 * or <code>asyncExec()</code>) are waiting to be processed, and if
 * so handles them before returning.
 * </p>
 *
 * @return <code>false</code> if the caller can sleep upon return from this method
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_FAILED_EXEC - if an exception occurred while running an inter-thread message</li>
 * </ul>
 *
 * @see #sleep
 * @see #wake
 */
public boolean readAndDispatch () {
	checkDevice ();
	application.run ();
//	application.nextEventMatchingMask(mask, expiration, mode, deqFlag)
	return true;
}

static void register (Display display) {
	synchronized (Device.class) {
		for (int i=0; i<Displays.length; i++) {
			if (Displays [i] == null) {
				Displays [i] = display;
				return;
			}
		}
		Display [] newDisplays = new Display [Displays.length + 4];
		System.arraycopy (Displays, 0, newDisplays, 0, Displays.length);
		newDisplays [Displays.length] = display;
		Displays = newDisplays;
	}
}

/**
 * Releases any internal resources back to the operating
 * system and clears all fields except the device handle.
 * <p>
 * Disposes all shells which are currently open on the display. 
 * After this method has been invoked, all related related shells
 * will answer <code>true</code> when sent the message
 * <code>isDisposed()</code>.
 * </p><p>
 * When a device is destroyed, resources that were acquired
 * on behalf of the programmer need to be returned to the
 * operating system.  For example, if the device allocated a
 * font to be used as the system font, this font would be
 * freed in <code>release</code>.  Also,to assist the garbage
 * collector and minimize the amount of memory that is not
 * reclaimed when the programmer keeps a reference to a
 * disposed device, all fields except the handle are zero'd.
 * The handle is needed by <code>destroy</code>.
 * </p>
 * This method is called before <code>destroy</code>.
 * 
 * @see Device#dispose
 * @see #destroy
 */
protected void release () {
	disposing = true;
	sendEvent (SWT.Dispose, new Event ());
	Shell [] shells = getShells ();
	for (int i=0; i<shells.length; i++) {
		Shell shell = shells [i];
		if (!shell.isDisposed ()) shell.dispose ();
	}
	if (tray != null) tray.dispose ();
	tray = null;
//	while (readAndDispatch ()) {}
	if (disposeList != null) {
		for (int i=0; i<disposeList.length; i++) {
			if (disposeList [i] != null) disposeList [i].run ();
		}
	}
	disposeList = null;
	synchronizer.releaseSynchronizer ();
	synchronizer = null;
	releaseDisplay ();
	super.release ();
}

void releaseDisplay () {	
	/* Release the System Images */
	if (errorImage != null) errorImage.dispose ();
	if (infoImage != null) infoImage.dispose ();
	if (warningImage != null) warningImage.dispose ();
	errorImage = infoImage = warningImage = null;
	
	/* Release the System Cursors */
	for (int i = 0; i < cursors.length; i++) {
		if (cursors [i] != null) cursors [i].dispose ();
	}
	cursors = null;

	if (windowDelegateCallback2 != null) windowDelegateCallback2.dispose ();
	if (windowDelegateCallback3 != null) windowDelegateCallback3.dispose ();
	if (windowDelegateCallback3 != null) windowDelegateCallback4.dispose ();
	if (windowDelegateCallback3 != null) windowDelegateCallback5.dispose ();
	if (dialogCallback3 != null) dialogCallback3.dispose ();
	windowDelegateCallback2 = windowDelegateCallback3 = windowDelegateCallback4 = windowDelegateCallback5 = null;
}

/**
 * Removes the listener from the collection of listeners who will
 * be notified when an event of the given type occurs anywhere in
 * a widget. The event type is one of the event constants defined
 * in class <code>SWT</code>.
 *
 * @param eventType the type of event to listen for
 * @param listener the listener which should no longer be notified when the event occurs
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 * </ul>
 *
 * @see Listener
 * @see SWT
 * @see #addFilter
 * @see #addListener
 * 
 * @since 3.0
 */
public void removeFilter (int eventType, Listener listener) {
	checkDevice ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (filterTable == null) return;
	filterTable.unhook (eventType, listener);
	if (filterTable.size () == 0) filterTable = null;
}

/**
 * Removes the listener from the collection of listeners who will
 * be notified when an event of the given type occurs. The event type
 * is one of the event constants defined in class <code>SWT</code>.
 *
 * @param eventType the type of event to listen for
 * @param listener the listener which should no longer be notified when the event occurs
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 *
 * @see Listener
 * @see SWT
 * @see #addListener
 * 
 * @since 2.0 
 */
public void removeListener (int eventType, Listener listener) {
	checkDevice ();
	if (listener == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (eventTable == null) return;
	eventTable.unhook (eventType, listener);
}

//void removeMenu (Menu menu) {
//	if (menus == null) return;
//	menus [menu.id - ID_START] = null;
//}
//
//void removePopup (Menu menu) {
//	if (popups == null) return;
//	for (int i=0; i<popups.length; i++) {
//		if (popups [i] == menu) {
//			popups [i] = null;
//			return;
//		}
//	}
//}

boolean runAsyncMessages (boolean all) {
	return synchronizer.runAsyncMessages (all);
}

boolean runDeferredEvents () {
	/*
	* Run deferred events.  This code is always
	* called  in the Display's thread so it must
	* be re-enterant need not be synchronized.
	*/
	while (eventQueue != null) {
		
		/* Take an event off the queue */
		Event event = eventQueue [0];
		if (event == null) break;
		int length = eventQueue.length;
		System.arraycopy (eventQueue, 1, eventQueue, 0, --length);
		eventQueue [length] = null;

		/* Run the event */
		Widget widget = event.widget;
		if (widget != null && !widget.isDisposed ()) {
			Widget item = event.item;
			if (item == null || !item.isDisposed ()) {
				widget.notifyListeners (event.type, event);
			}
		}

		/*
		* At this point, the event queue could
		* be null due to a recursive invokation
		* when running the event.
		*/
	}

	/* Clear the queue */
	eventQueue = null;
	return true;
}

void sendEvent (int eventType, Event event) {
	if (eventTable == null && filterTable == null) {
		return;
	}
	if (event == null) event = new Event ();
	event.display = this;
	event.type = eventType;
	if (event.time == 0) event.time = getLastEventTime ();
	if (!filterEvent (event)) {
		if (eventTable != null) eventTable.sendEvent (event);
	}
}

/**
 * On platforms which support it, sets the application name
 * to be the argument. On Motif, for example, this can be used
 * to set the name used for resource lookup.  Specifying
 * <code>null</code> for the name clears it.
 *
 * @param name the new app name or <code>null</code>
 */
public static void setAppName (String name) {
	APP_NAME = name;
}

/**
 * Sets the location of the on-screen pointer relative to the top left corner
 * of the screen.  <b>Note: It is typically considered bad practice for a
 * program to move the on-screen pointer location.</b>
 *
 * @param x the new x coordinate for the cursor
 * @param y the new y coordinate for the cursor
 * 
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 * 
 * @since 2.1
 */
public void setCursorLocation (int x, int y) {
	checkDevice ();
//	CGPoint pt = new CGPoint ();
//	pt.x = x;  pt.y = y;
//	OS.CGWarpMouseCursorPosition (pt);
}

/**
 * Sets the location of the on-screen pointer relative to the top left corner
 * of the screen.  <b>Note: It is typically considered bad practice for a
 * program to move the on-screen pointer location.</b>
 *
 * @param point new position
 * 
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 *    <li>ERROR_NULL_ARGUMENT - if the point is null
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 * 
 * @since 2.0
 */
public void setCursorLocation (Point point) {
	checkDevice ();
	if (point == null) error (SWT.ERROR_NULL_ARGUMENT);
	setCursorLocation (point.x, point.y);
}

/**
 * Sets the application defined property of the receiver
 * with the specified name to the given argument.
 * <p>
 * Applications may have associated arbitrary objects with the
 * receiver in this fashion. If the objects stored in the
 * properties need to be notified when the display is disposed
 * of, it is the application's responsibility provide a
 * <code>disposeExec()</code> handler which does so.
 * </p>
 *
 * @param key the name of the property
 * @param value the new value for the property
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the key is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 *
 * @see #getData(String)
 * @see #disposeExec(Runnable)
 */
public void setData (String key, Object value) {
	checkDevice ();
	if (key == null) error (SWT.ERROR_NULL_ARGUMENT);
	
	/* Remove the key/value pair */
	if (value == null) {
		if (keys == null) return;
		int index = 0;
		while (index < keys.length && !keys [index].equals (key)) index++;
		if (index == keys.length) return;
		if (keys.length == 1) {
			keys = null;
			values = null;
		} else {
			String [] newKeys = new String [keys.length - 1];
			Object [] newValues = new Object [values.length - 1];
			System.arraycopy (keys, 0, newKeys, 0, index);
			System.arraycopy (keys, index + 1, newKeys, index, newKeys.length - index);
			System.arraycopy (values, 0, newValues, 0, index);
			System.arraycopy (values, index + 1, newValues, index, newValues.length - index);
			keys = newKeys;
			values = newValues;
		}
		return;
	}
	
	/* Add the key/value pair */
	if (keys == null) {
		keys = new String [] {key};
		values = new Object [] {value};
		return;
	}
	for (int i=0; i<keys.length; i++) {
		if (keys [i].equals (key)) {
			values [i] = value;
			return;
		}
	}
	String [] newKeys = new String [keys.length + 1];
	Object [] newValues = new Object [values.length + 1];
	System.arraycopy (keys, 0, newKeys, 0, keys.length);
	System.arraycopy (values, 0, newValues, 0, values.length);
	newKeys [keys.length] = key;
	newValues [values.length] = value;
	keys = newKeys;
	values = newValues;
}

/**
 * Sets the application defined, display specific data
 * associated with the receiver, to the argument.
 * The <em>display specific data</em> is a single,
 * unnamed field that is stored with every display. 
 * <p>
 * Applications may put arbitrary objects in this field. If
 * the object stored in the display specific data needs to
 * be notified when the display is disposed of, it is the
 * application's responsibility provide a
 * <code>disposeExec()</code> handler which does so.
 * </p>
 *
 * @param data the new display specific data
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 *
 * @see #getData()
 * @see #disposeExec(Runnable)
 */
public void setData (Object data) {
	checkDevice ();
	this.data = data;
}

/**
 * Sets the synchronizer used by the display to be
 * the argument, which can not be null.
 *
 * @param synchronizer the new synchronizer for the display (must not be null)
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the synchronizer is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 *    <li>ERROR_FAILED_EXEC - if an exception occurred while running an inter-thread message</li>
 * </ul>
 */
public void setSynchronizer (Synchronizer synchronizer) {
	checkDevice ();
	if (synchronizer == null) error (SWT.ERROR_NULL_ARGUMENT);
	if (synchronizer == this.synchronizer) return;
	Synchronizer oldSynchronizer;
	synchronized (Device.class) {
		oldSynchronizer = this.synchronizer;
		this.synchronizer = synchronizer;
	}
	if (oldSynchronizer != null) {
		oldSynchronizer.runAsyncMessages(true);
	}
}

/**
 * Causes the user-interface thread to <em>sleep</em> (that is,
 * to be put in a state where it does not consume CPU cycles)
 * until an event is received or it is otherwise awakened.
 *
 * @return <code>true</code> if an event requiring dispatching was placed on the queue.
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 *
 * @see #wake
 */
public boolean sleep () {
	checkDevice ();
	if (getMessageCount () != 0) return true;
	return false;
}

int sourceProc (int info) {
	return 0;
}

/**
 * Causes the <code>run()</code> method of the runnable to
 * be invoked by the user-interface thread at the next 
 * reasonable opportunity. The thread which calls this method
 * is suspended until the runnable completes.  Specifying <code>null</code>
 * as the runnable simply wakes the user-interface thread.
 * <p>
 * Note that at the time the runnable is invoked, widgets 
 * that have the receiver as their display may have been
 * disposed. Therefore, it is necessary to check for this
 * case inside the runnable before accessing the widget.
 * </p>
 * 
 * @param runnable code to run on the user-interface thread or <code>null</code>
 *
 * @exception SWTException <ul>
 *    <li>ERROR_FAILED_EXEC - if an exception occurred when executing the runnable</li>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 *
 * @see #asyncExec
 */
public void syncExec (Runnable runnable) {
	Synchronizer synchronizer;
	synchronized (Device.class) {
		if (isDisposed ()) error (SWT.ERROR_DEVICE_DISPOSED);
		synchronizer = this.synchronizer;
	}
	synchronizer.syncExec (runnable);
}

/**
 * Causes the <code>run()</code> method of the runnable to
 * be invoked by the user-interface thread after the specified
 * number of milliseconds have elapsed. If milliseconds is less
 * than zero, the runnable is not executed.
 * <p>
 * Note that at the time the runnable is invoked, widgets 
 * that have the receiver as their display may have been
 * disposed. Therefore, it is necessary to check for this
 * case inside the runnable before accessing the widget.
 * </p>
 *
 * @param milliseconds the delay before running the runnable
 * @param runnable code to run on the user-interface thread
 *
 * @exception IllegalArgumentException <ul>
 *    <li>ERROR_NULL_ARGUMENT - if the runnable is null</li>
 * </ul>
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 *
 * @see #asyncExec
 */
public void timerExec (int milliseconds, Runnable runnable) {
	checkDevice ();
//	if (runnable == null) error (SWT.ERROR_NULL_ARGUMENT);
//	if (timerList == null) timerList = new Runnable [4];
//	if (timerIds == null) timerIds = new int [4];
//	int index = 0;
//	while (index < timerList.length) {
//		if (timerList [index] == runnable) break;
//		index++;
//	}
//	if (index != timerList.length) {
//		int timerId = timerIds [index];
//		if (milliseconds < 0) {
//			OS.RemoveEventLoopTimer (timerId);
//			timerList [index] = null;
//			timerIds [index] = 0;
//		} else {
//			OS.SetEventLoopTimerNextFireTime (timerId, milliseconds / 1000.0);
//		}
//		return;
//	} 
//	if (milliseconds < 0) return;
//	index = 0;
//	while (index < timerList.length) {
//		if (timerList [index] == null) break;
//		index++;
//	}
//	if (index == timerList.length) {
//		Runnable [] newTimerList = new Runnable [timerList.length + 4];
//		System.arraycopy (timerList, 0, newTimerList, 0, timerList.length);
//		timerList = newTimerList;
//		int [] newTimerIds = new int [timerIds.length + 4];
//		System.arraycopy (timerIds, 0, newTimerIds, 0, timerIds.length);
//		timerIds = newTimerIds;
//	}
//	int [] timerId = new int [1];
//	int eventLoop = OS.GetCurrentEventLoop ();
//	OS.InstallEventLoopTimer (eventLoop, milliseconds / 1000.0, 0.0, timerProc, index, timerId);
//	if (timerId [0] != 0) {
//		timerIds [index] = timerId [0];
//		timerList [index] = runnable;
//	}
}

/**
 * Forces all outstanding paint requests for the display
 * to be processed before this method returns.
 *
 * @exception SWTException <ul>
 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 * 
 * @see Control#update()
 */
public void update () {
	checkDevice ();	
//	Shell [] shells = getShells ();
//	for (int i=0; i<shells.length; i++) {
//		Shell shell = shells [i];
//		if (!shell.isDisposed ()) shell.update (true);
//	}

}

/**
 * If the receiver's user-interface thread was <code>sleep</code>ing, 
 * causes it to be awakened and start running again. Note that this
 * method may be called from any thread.
 * 
 * @exception SWTException <ul>
 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
 * </ul>
 * 
 * @see #sleep
 */
public void wake () {
	synchronized (Device.class) {
		if (isDisposed ()) error (SWT.ERROR_DEVICE_DISPOSED);
		if (thread == Thread.currentThread ()) return;
		wakeThread ();
	}
}

void wakeThread () {
}

int dialogProc(int id, int sel, int arg0) {
	int jniRef = OS.objc_msgSend(id, OS.sel_tag);
	if (jniRef == 0 || jniRef == -1) return 0;
	if (sel == OS.sel_changeColor_1) {
		ColorDialog dialog = (ColorDialog)OS.JNIGetObject(jniRef);
		if (dialog == null) return 0;
		dialog.changeColor(arg0);
	} else if (sel == OS.sel_changeFont_1) {
		FontDialog dialog = (FontDialog)OS.JNIGetObject(jniRef);
		if (dialog == null) return 0;
		dialog.changeFont(arg0);
	} else if (sel == OS.sel_windowWillClose_1) {
		Object object = OS.JNIGetObject(jniRef);
		if (object instanceof FontDialog) {
			((FontDialog)object).windowWillClose(arg0);
		} else if (object instanceof ColorDialog) {
			((ColorDialog)object).windowWillClose(arg0);
		}
	}
	return 0;
}

int windowDelegateProc(int delegate, int sel) {
	if (sel == OS.sel_tag) {
		int[] tag = new int[1];
		OS.object_getInstanceVariable(delegate, "tag", tag);	
		return tag[0];
	}
	int jniRef = OS.objc_msgSend(delegate, OS.sel_tag);
	if (jniRef == 0 || jniRef == -1) return 0;
	Widget widget = (Widget)OS.JNIGetObject(jniRef);
	if (widget == null) return 0;
	if (sel == OS.sel_isFlipped) {
		return widget.isFlipped() ? 1 : 0;
	}
	if (sel == OS.sel_sendSelection) {
		widget.sendSelection();
		return 0;
	}
	if (sel == OS.sel_sendDoubleSelection) {
		widget.sendDoubleSelection();
		return 0;
	}
	return 0;
}

int windowDelegateProc(int delegate, int sel, int arg0) {
	if (sel == OS.sel_setTag_1) {
		OS.object_setInstanceVariable(delegate, "tag", arg0);
		return 0;
	}
	int jniRef = OS.objc_msgSend(delegate, OS.sel_tag);
	if (jniRef == 0 || jniRef == -1) return 0;
	Widget widget = (Widget)OS.JNIGetObject(jniRef);
	if (widget == null) return 0;
	if (sel == OS.sel_windowWillClose_1) {
		widget.windowWillClose(arg0);
	} else if (sel == OS.sel_drawRect_1) {
		NSRect rect = new NSRect();
		OS.memmove(rect, arg0, NSRect.sizeof);
		objc_super super_struct = new objc_super();
		super_struct.receiver = delegate;
		super_struct.cls = OS.objc_msgSend(delegate, OS.sel_superclass);
		widget.preDrawRect(rect);
		OS.objc_msgSendSuper(super_struct, OS.sel_drawRect_1, rect);
		widget.drawRect(rect);
	} else if (sel == OS.sel_windowShouldClose_1) {
		return widget.windowShouldClose(arg0) ? 1 : 0;
	} else if (sel == OS.sel_mouseDown_1) {
		widget.mouseDown(arg0);
	} else if (sel == OS.sel_rightMouseDown_1) {
		widget.rightMouseDown(arg0);
	} else if (sel == OS.sel_mouseUp_1) {
		widget.mouseUp(arg0);
	} else if (sel == OS.sel_keyDown_1) {
		widget.keyDown(arg0);
	} else if (sel == OS.sel_numberOfRowsInTableView_1) {
		return widget.numberOfRowsInTableView(arg0);
	} else if (sel == OS.sel_comboBoxSelectionDidChange_1) {
		widget.comboBoxSelectionDidChange(arg0);
	} else if (sel == OS.sel_tableViewSelectionDidChange_1) {
		widget.tableViewSelectionDidChange(arg0);
	} else if (sel == OS.sel_windowDidResize_1) {
		widget.windowDidResize(arg0);
	} else if (sel == OS.sel_windowDidMove_1) {
		widget.windowDidMove(arg0);
	} else if (sel == OS.sel_menuForEvent_1) {
		return widget.menuForEvent(arg0);
	}
	return 0;
}


int windowDelegateProc(int delegate, int sel, int arg0, int arg1) {
	int jniRef = OS.objc_msgSend(delegate, OS.sel_tag);
	if (jniRef == 0 || jniRef == -1) return 0;
	Widget widget = (Widget)OS.JNIGetObject(jniRef);
	if (widget == null) return 0;
	if (sel == OS.sel_tabView_1willSelectTabViewItem_1) {
		widget.willSelectTabViewItem(arg0, arg1);
	}
	return 0;
}


int windowDelegateProc(int delegate, int sel, int arg0, int arg1, int arg2) {
	if (sel == OS.sel_setTag_1) {
		OS.object_setInstanceVariable(delegate, "tag", arg0);
		return 0;
	}
	int jniRef = OS.objc_msgSend(delegate, OS.sel_tag);
	if (jniRef == 0 || jniRef == -1) return 0;
	Widget widget = (Widget)OS.JNIGetObject(jniRef);
	if (widget == null) return 0;
	if (sel == OS.sel_tableView_1objectValueForTableColumn_1row_1) {
		return widget.tableViewobjectValueForTableColumnrow(arg0, arg1, arg2);
	}
	if (sel == OS.sel_tableView_1shouldEditTableColumn_1row_1) {
		return widget.tableViewshouldEditTableColumnrow(arg0, arg1, arg2) ? 1 : 0;
	}
	return 0;
}
}
