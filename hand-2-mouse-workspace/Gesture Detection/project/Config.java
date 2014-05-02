/**
 * 
 */
package project;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.opencv.core.Core;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import project.gui.common.GestureDialog;
import project.recognition.AbstractGesture;
import wizard.gui.ConfigWizard;
import wizard.gui.StepPanel;
import custom.javax.swing.layout.TableLayout;

/**
 * 
 * 
 * @author Christopher N. Hartley
 *
 * @see java.lang.Runnable
 */
public class Config implements Runnable {

	// Private member data.
	private final String filePath;         // XML Document file path
	private Document xmlConfigDoc = null;  // XML Document
	private final DocumentBuilderFactory docFactory;
	private final DocumentBuilder docBuilder;
	
	
	/**
	 * Constructor for a new instance of this configuration object with the
	 * specified path to the configuration file.
	 * 
	 * @param filePath	the {@link String} path to the configuration file.
	 */
	public Config(String filePath) {
		this.filePath = filePath;
		
		docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = null;
		try {
			db = docFactory.newDocumentBuilder();
		}
		catch (ParserConfigurationException ignore) {	}
		finally {
			docBuilder = db;
		}
		
		File xmlFile = new File(filePath);
		if (xmlFile.exists() && xmlFile.isFile())
			loadConfigFile(xmlFile);
	}
	
	
	/**
	 * 
	 * @param xmlFile
	 */
	private final void loadConfigFile(File xmlFile) {
		try {
			docBuilder.reset();
			xmlConfigDoc = docBuilder.parse(xmlFile);
			xmlConfigDoc.normalizeDocument();
		}
		catch (SAXException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Upon running this instance of the {@link Config} object, this method
	 * launches the configuration wizard to walk the user through the setup for
	 * the application.
	 */
	@Override
	public void run() {
		launchConfigWizard();
	}
	
	
	/**
	 * Returns the OpenCV library path to the native system library for the 
	 * user's computer system. This path should be to the 
	 * {@link org.opencv.core.Core#NATIVE_LIBRARY_NAME} file in order for this
	 * application to function with OpenCV. If this configuration setting is 
	 * not set, this method returns {@code null}.
	 * 
	 * @return	the {@link String} absolute path to the OpenCV native library
	 * 			file on the users' computer.
	 */
	public final String getOpenCVlibPath() {
		if (xmlConfigDoc == null)
			return null;
		
		Node node = getFirstNodeByTagName("opencv_lib", xmlConfigDoc);
		if (node != null && node.getFirstChild() != null)
			return node.getFirstChild().getNodeValue();
		else
			return null;
	}
	
	
	/**
	 * Returns {@code true} if the file path to the configuration file is not
	 * {@code null}, the file exists, and the path specifies a readable file;
	 * otherwise, returns {@code false}.
	 * 
	 * @return	{@code true} if the configuration file is accessible; 
	 * 			otherwise, returns {@code false}.
	 */
	public final boolean checkIfConfigFileExists() {
		if (filePath == null)
			return false;
		
		File f = new File(filePath);
		return f.exists() && f.isFile();
	}
	
	
	/**
	 * This method creates a new XML document and passes it to a new instance 
	 * of the {@link ProjectConfigWizard}. This new XML document can then 
	 * become the main configuration file if the user completes all of the 
	 * steps in the configuration wizard. The wizard acts as a modal dialog to
	 * stay on top of this application and block until the user cancels or 
	 * completes the process.
	 */
	private final void launchConfigWizard() {
		Document doc = null;
		
		if (checkIfConfigFileExists())
			doc = xmlConfigDoc;
		else
			doc = docBuilder.newDocument();
			
		new ProjectConfigWizard(doc).start();		
	}
	

	/**
	 * Convenience method to return the first child {@link Node} of the specified
	 * node containing the tag name within the XML document, the {@code doc}
	 * parameter. If the tag name cannot be found within the document, this
	 * returns {@code null}.
	 * 
	 * @param tagName	the XML tag name to capture and return its first child
	 * 					{@link Node} for.
	 * @param doc		the XML document to search for the specified tag name.
	 * 
	 * @return			the first child {@link Node} for the specified node by
	 * 					the tag name.
	 */
	private final Node getFirstNodeByTagName(String tagName, Document doc) {
		NodeList nodes = doc.getElementsByTagName(tagName);
		if (nodes == null || nodes.getLength() == 0)
			return null;
		else
			return nodes.item(0);
	}
	
	
	/**
	 * Custom implementation of the {@link ConfigWizard} for this applications
	 * configuration file. This wizard utilizes a XML document as its parameter
	 * to store the steps of the wizard. Upon successfully completing this
	 * implementation of the wizard, the XML document is written as the new 
	 * configuration file to use in subsequent runs of this application.
	 * 
	 * @author Chris Hartley
	 * 
	 * @see org.w3c.dom.Document
	 * @see wizard.gui.ConfigWizard
	 */
	private class ProjectConfigWizard extends ConfigWizard<Document> {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 5571249608594733181L;


		/**
		 * Constructor for a new instance of this project configuration wizard
		 * with the specified XML {@link Document}.
		 * 
		 * @param doc	the XML {@link Document} to store the individual steps
		 * 				of the wizard in.
		 */
		public ProjectConfigWizard(Document doc) {
			super("Gesture Configuration Wizard", doc);

			setSize(480, 320);
			setModalityType(DEFAULT_MODALITY_TYPE);
			
			addStep("Intro", new Step1());
			addStep("Open CV", new Step2());
			addStep("Classifier(s)", new Step3());
			addStep("Finalize", new Step4());
		}
		
		
		/**
		 * Once the user successfully completes all of the steps for this 
		 * instance of the {@link ProjectConfigWizard}, this method is called
		 * to write the XML {@link Document} to the configuration file.
		 * 
		 * @param doc	the XML {link Document} to be written as the new
		 * 				configuration file.
		 */
		@Override
		public void finished(Document doc) {
			try {
				TransformerFactory transformerFactory =
						TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				
				File xmlFile = new File(filePath);
				if (xmlFile.exists())
					xmlFile.delete();
				
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(xmlFile);
				
				transformer.transform(source, result);
				loadConfigFile(xmlFile);
			}
			catch (TransformerException e) {
				e.printStackTrace();
			}
		}
		
		
		/**
		 * Removes all children {@link Node}s from the specified {@link Node}.
		 * 
		 * @param node	the {@link Node} to have all of its children removed
		 * 				from.
		 */
		private void clearNodeChildren(Node node) {
			if (node.hasChildNodes()) {
				NodeList tmpList = node.getChildNodes();
				for (int i = 0; i < tmpList.getLength(); i++)
					node.removeChild(tmpList.item(i));
			}
		}
		
		
		/**
		 * The first step of the configuration wizard which acts as an 
		 * introduction page to the configuration wizard.
		 * 
		 * @author Chris Hartley
		 *
		 */
		public class Step1 extends StepPanel<Document> {

			/**
			 * 
			 */
			private static final long serialVersionUID = -9122444186190837042L;
			private static final String description =
					"This configuration wizard will guide you through the required information in order to setup this application for use.\n\n"
					+ "You may want to lookup the folder location of your installation for OpenCV 2.4.8 on your computer before continuing. If you have not downloaded and installed OpenCV 2.4.8 at this time, please close out of this application and go to the web site http://opencv.org/"
					+ "\n\n"
					+ "Click 'Next' to begin...";
			
			private boolean hasInitialized = false;
			
			public Step1() {
				super(null);

				BoxLayout box = new BoxLayout(this, BoxLayout.Y_AXIS);
				setLayout(box);
				
				JTextArea txt = new JTextArea(description);
				txt.setFont(ProjectConfigWizard.this.getFont());
				txt.setOpaque(false);
				txt.setWrapStyleWord(true);
				txt.setLineWrap(true);
				txt.setEditable(false);
				txt.setFocusable(false);
				add(txt);
				
				add(Box.createVerticalGlue());
			}

			@Override
			public void initialize(Document doc) throws DOMException {
				if (hasInitialized)
					return;
				
				Element rootElement;
				String tagName = "configuration";
				if (doc.getElementsByTagName(tagName).getLength() == 0) {
					rootElement = doc.createElement(tagName);
					doc.appendChild(rootElement);
				
					Element filesElement = doc.createElement("files");
					rootElement.appendChild(filesElement);
					
					filesElement.appendChild(doc.createElement("opencv_lib"));
				
					Element gestures = doc.createElement("gestures");
					rootElement.appendChild(gestures);
				}
				hasInitialized = true;
			}

			@Override
			public void previous(Document doc) { }

			@Override
			public void next(Document doc) { }
			
		}
		
		
		/**
		 * The second step of the configuration wizard which request for the 
		 * user to specify the location of the native library for OpenCV.
		 * 
		 * @author Chris Hartley
		 *
		 */
		public class Step2 extends StepPanel<Document>
				implements ActionListener
		{

			/**
			 * 
			 */
			private static final long serialVersionUID = -5882293265222818658L;

			final JTextField field = new JTextField(25);
			final JFileChooser fileChooser;
			
			public Step2() {
				super( new BorderLayout() );

				JLabel lbl;
				JTextArea txt = new JTextArea("This application uses Open CV to "
						+ "interface with your web camera and handle the image "
						+ "processing. Please specify the location of your "
						+ "installation of Open CV. If you have not installed Open CV "
						+ "at this time, you should visit www.opencv.org and download "
						+ "the appropriate version for your computer system.");
				txt.setFont(ProjectConfigWizard.this.getFont());
				txt.setOpaque(false);
				txt.setWrapStyleWord(true);
				txt.setLineWrap(true);
				txt.setEditable(false);
				txt.setFocusable(false);
				add(txt, BorderLayout.CENTER);
				
				JPanel pnl = new JPanel(null);
				pnl.setLayout( new TableLayout(2, 3) );
				pnl.setOpaque(false);
				lbl = new JLabel("Path to '" + Core.NATIVE_LIBRARY_NAME + "'",
						SwingConstants.RIGHT);
				pnl.add(lbl);
				pnl.add(field);
				lbl.setLabelFor(field);
				
				JButton btn = new JButton("F");
				btn.addActionListener(this);
				pnl.add(btn);
				
				pnl.add( lbl = new JLabel("example", SwingConstants.RIGHT) );
				lbl.setForeground(getBackground().darker());
				lbl = new JLabel("C:/OpenCV/build/java/x64/"
						+ Core.NATIVE_LIBRARY_NAME + ".dll") ;
				pnl.add(lbl);
				lbl.setForeground(getBackground().darker());
				pnl.add( new JLabel("") );
				add(pnl, BorderLayout.SOUTH);
				
				fileChooser = new JFileChooser();
				fileChooser.setCurrentDirectory(null);
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setMultiSelectionEnabled(false);
			}

			@Override
			public void actionPerformed(ActionEvent ae) {
				int ok = fileChooser.showOpenDialog(ProjectConfigWizard.this);
				
				if (ok == JFileChooser.APPROVE_OPTION) {
					File f = fileChooser.getSelectedFile();
					field.setText("" + f.getAbsolutePath());
				}
			}

			@Override
			public void initialize(Document doc) {
				Node node = getFirstNodeByTagName("opencv_lib", doc);
				if (node != null && node.getFirstChild() != null)
					field.setText("" + node.getFirstChild().getNodeValue());
				else
					field.setText("[none]");
					
			}

			@Override
			public void previous(Document doc) { }

			@Override
			public void next(Document doc) {
				Node node = getFirstNodeByTagName("opencv_lib", doc);
				
				if (node == null) {
					System.err.println("XML Document not initialized correctly!");
					System.exit(1);
				}
				
				clearNodeChildren(node);
				node.appendChild(doc.createTextNode(field.getText()));
			}
			
		}
		
		
		/**
		 * The third step of the configuration wizard which asks if there are
		 * any classifiers to pre-register with the application in the 
		 * configuration file.
		 * 
		 * @author Chris Hartley
		 *
		 */
		public class Step3 extends StepPanel<Document>
				implements ActionListener, PropertyChangeListener
		{

			private static final long serialVersionUID = -2636235113559610915L;

			private final LinkedList<AbstractGesture> gestures =
					new LinkedList<AbstractGesture>();
			
			
			/**
			 * 
			 */
			public Step3() {
				super(null);

				BoxLayout box = new BoxLayout(this, BoxLayout.Y_AXIS);
				setLayout(box);
				
				JTextArea txt = new JTextArea("Inorder to detect your hand gestures, "
						+ "please register any classifiers that you may select now or "
						+ "later to detect your hand gestures.");
				txt.setFont(ProjectConfigWizard.this.getFont());
				txt.setOpaque(false);
				txt.setWrapStyleWord(true);
				txt.setLineWrap(true);
				txt.setEditable(false);
				txt.setFocusable(false);
				add(txt);
				
				add(Box.createVerticalGlue());
				
				JButton btn = new JButton("Add Gesture...");
				btn.setActionCommand("add-new-gesture-action");
				btn.addActionListener(this);
				add(btn);
			}

			@Override
			public void initialize(Document doc) {
				//TODO: Load the already specified classifiers from the XML
				//      document if it contains any.
				gestures.clear();
			}

			@Override
			public void previous(Document doc) { }

			@Override
			public void next(Document doc) {
				Node gestures = getFirstNodeByTagName("gestures", doc);
				
				//TODO: get the list of classifiers specified by the user...
				for (int i = 0; i < 0; i++) {
					Element gesture = doc.createElement("gesture");
					gesture.setAttribute("name", "Fist 00");
					gesture.setAttribute("color", "#FF0000");
					gesture.setAttribute("enabled", "true");
						Element classifier = doc.createElement("classifier");
						classifier.appendChild(doc.createTextNode("/classifiers/fist_classifier.xml"));
						gesture.appendChild(classifier);
					gestures.appendChild(gesture);
				}
				
				/*
				gr.registerGesture("Fist 00", GestureEvent.CLOSED_HAND_DETECTED,
						gr.loadClassifier("../classifiers/fist_classifier.xml"),
						Color.RED, robot).setEnabled(true); // Pretty good fist detection!
				
				gr.registerGesture("Closed Palm 00", GestureEvent.CLOSED_HAND_DETECTED,
						gr.loadClassifier("../classifiers/closed_palm_classifier.xml"),
						Color.CYAN, robot).setEnabled(false); // Picks up opened hand (figures)!
				
				gr.registerGesture("Fist 01", GestureEvent.CLOSED_HAND_DETECTED,
						gr.loadClassifier("../classifiers/fist.xml"),
						Color.YELLOW, robot).setEnabled(false);
				
				gr.registerGesture("Hand 01", GestureEvent.OPENED_HAND_DETECTED,
						gr.loadClassifier("../classifiers/hand_classifier_1.xml"),
						Color.GREEN, robot).setEnabled(false); // Finds lots of random objects!
		
				gr.registerGesture("Hand 02", GestureEvent.OPENED_HAND_DETECTED,
						gr.loadClassifier("../classifiers/hand_classifier_2.xml"),
						Color.GREEN.brighter(), robot).setEnabled(false); // Finds random stuff, not hands!
				
				gr.registerGesture("Palm 01", GestureEvent.OPENED_HAND_DETECTED,
						gr.loadClassifier("../classifiers/palm.xml"),
						Color.ORANGE, robot).setEnabled(true);
				*/
			}

			@Override
			public void actionPerformed(ActionEvent ae) {
				String cmd = ae.getActionCommand();
				
				if (cmd == null)
					return;
				
				if (cmd.equals("add-new-gesture-action")) {
					GestureDialog gd =
							new GestureDialog(ProjectConfigWizard.this);
					
					gd.addPropertyChangeListener(this);
					gd.setVisible(true);
				}
				
			}

			@Override
			public void propertyChange(PropertyChangeEvent pce) {
				String prop = pce.getPropertyName();
				
				if (GestureDialog.SAVE_ACTION_COMMAND.equals(prop)) {
					System.out.println("Property Changed: " + pce);
				}
				else if (GestureDialog.CANCEL_ACTION_COMMAND.equals(prop)) {
					System.out.println("Property Changed: " + pce);
				}
			}
			
		}//*/
		
		
		/**
		 * The final step in the configuration wizard which summarizes the new
		 * setting specified by the user and informs the user to click "Finish"
		 * in order for the configurations to be saved to the configurations 
		 * file.
		 * 
		 * @author Chris Hartley
		 *
		 */
		public class Step4 extends StepPanel<Document> {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1626558062192967763L;

			public Step4() {
				super(null);

				BoxLayout box = new BoxLayout(this, BoxLayout.Y_AXIS);
				setLayout(box);
				
				JTextArea txt = new JTextArea("Configuration is now complete. "
						+ "OpenCV is registered at: [opencv-path].\n"
						+ "You have [num] calssifiers registered for the geture"
						+ " detection.");
				txt.setFont(ProjectConfigWizard.this.getFont());
				txt.setOpaque(false);
				txt.setWrapStyleWord(true);
				txt.setLineWrap(true);
				txt.setEditable(false);
				txt.setFocusable(false);
				add(txt);
				
				add(Box.createVerticalGlue());
			}

			@Override
			public void initialize(Document obj) {
				//TODO: collect the setting and configurations from the XML 
				//      document to display as a review before completing this
				//      configuration wizard...
			}

			@Override
			public void previous(Document obj) { }

			@Override
			public void next(Document obj) { }
			
		}

	}

	
}
