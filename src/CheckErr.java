import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class CheckErr {
	String[] lines;
	StringBuffer sb;
	boolean isRight;
	int lineNumber;
	int level;

	public CheckErr(int level) {
		this.level = level;
	}

	// errorHeader contains the information of the file to be checked
	public boolean checkIfRight(String code, String errorHeader) {
		sb = new StringBuffer();
		sb.append(errorHeader + "\n");
		isRight = true;
		lines = code.trim().split("\n");
		lineNumber = 0;
		int loops = 0;
		while (lineNumber < lines.length) {
			if (lines[lineNumber].startsWith("//")) {
				lineNumber++;
				continue;
			}
			if (lines[lineNumber].equals("")) {
				lineNumber++;
				continue;
			}
			String[] words = lines[lineNumber].trim().split(" ");
			words = removeEmpty(words);
			try {

				if (words[0].equals("process")) {
					if (Integer.parseInt(words[1]) == 1 && isDouble(words[2])
							&& isDouble(words[3]) && isDouble(words[4])
							&& Integer.parseInt(words[5]) < 41
							&& Integer.parseInt(words[5]) > 0
							&& Integer.parseInt(words[6]) < 41
							&& Integer.parseInt(words[6]) > 0
							&& words.length == 7)
						;
					else if (Integer.parseInt(words[1]) == 2
							&& isDouble(words[2]) && isDouble(words[3])
							&& isDouble(words[4])
							&& Integer.parseInt(words[5]) < 41
							&& Integer.parseInt(words[5]) > 0
							&& Integer.parseInt(words[6]) < 41
							&& (Integer.parseInt(words[6]) > 0)
							&& isDouble(words[7]) && words.length == 8)
						;
					else if (Integer.parseInt(words[1]) == 3
							&& isDouble(words[2]) && isDouble(words[3])
							&& Integer.parseInt(words[4]) < 41
							&& Integer.parseInt(words[4]) > 0
							&& Integer.parseInt(words[5]) < 41
							&& Integer.parseInt(words[5]) > 0
							&& isDouble(words[6]) && words.length == 7)
						;
					else if (Integer.parseInt(words[1]) == 4
							&& isDouble(words[2]) && words.length == 3)
						;

					else
						addError("process not recognized");
					lineNumber++;
					continue;
				}
				switch (words.length) {
				case 1:
					if (words[0].equals("wait"))
						break;
					else if (words[0].equals("}"))
						loops--;
					else
						addError("command not recognized");
					break;
				case 2:
					if ((words[0].equals("open")) || (words[0].equals("close"))) {
						String[] ports = words[1].split(",");
						for (int i = 0; i < ports.length; i++) {
							int portNumber = Integer.parseInt(ports[i]);
							if (portNumber > 0 && portNumber <= 40)
								continue;
							else
								addError("portNumber is out of range");
						}
					} else if (words[0].equals("wait")) {
						if (!isDouble(words[1]))
							addError("requires a number");
					}
					// import
					else if (words[0].equals("import")) {
						// check if file existes
						String path;
						if (OSValidator.isWindows()) {
							path = Configure.getCurrentFileParent() + "\\"
									+ words[1];
						}
						// linux
						else {
							path = Configure.getCurrentFileParent() + "/"
									+ words[1];
						}
						File f = new File(path);
						// check if file exists
						if (!f.exists()) {
							addError("file " + Configure.getCurrentFileName()
									+ " doesn't exists");
							break;
						}
						// read the file
						CheckErr ce2 = new CheckErr(this.level+1);
						ce2.checkIfRight(Lib.readFile(path), "import "
								+ words[1] + ":");
						if (!ce2.isRight) {
							sb.append(ce2.sb.toString());
							addError("file " + Configure.getCurrentFileName()
									+ " contains errors");
						}
						break;
					} else
						addError("command not recognized");
					break;
				case 3:
					if (words[0].equals("loop")
							&& Integer.parseInt(words[1]) > 0
							&& words[2].equals("{"))
						loops++;
					else if (words[0].endsWith("setTemp")
							&& (Integer.parseInt(words[1]) > 0)
							&& (Integer.parseInt(words[1]) < 3)
							&& isDouble(words[2]))
						;
					else if (words[0].equals("waitT3Until")
							&& (words[1].equals("<") || words[1].equals(">"))
							&& isDouble(words[2]))
						;

					else
						addError("command not recognized");
					break;
				}
			} catch (Exception e) {
				addError("exception caught");
			}
			lineNumber++;
		}
		if (loops != 0) {
			lineNumber = -1;
			addError("loop error");
		}
		return isRight;
	}

	private static String[] removeEmpty(String[] words) {
		ArrayList<String> al = new ArrayList<String>();
		for (int i = 0; i < words.length; i++) {
			al.add(words[i]);
		}
		int i = 0;
		while (i < al.size() - 1) {
			if (al.get(i).equals("")) {
				al.remove(i);
			} else {
				i++;
			}
		}
		return Arrays.copyOf(al.toArray(), al.size(), String[].class);
	}

	private static boolean isDouble(String string) {
		try {
			Double.parseDouble(string);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private void addError(String errorMessage) {
		for (int i = 0; i < level*4; i++) {
			sb.append(" ");
		}
		if (lineNumber != -1)
			sb.append("Error at line " + lineNumber + " : " + lines[lineNumber]
					+ "(" + errorMessage + ")" + "\n");
		else
			// loop error
			sb.append(errorMessage);
		isRight = false;
	}

}
