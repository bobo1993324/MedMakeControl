import java.io.File;
import java.io.IOException;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

public class Run implements Runnable {
	public static final int READ = 0;
	public static final int WRITE = 1;
	protected static double[] temperature = { -2, -2, -2 };
	protected static StringBuffer statusSb = new StringBuffer();
	protected static boolean isWaiting = false;
	String code;
	private Stack<Integer> loopTimes;
	private Stack<Integer> loopStart;
	public static boolean isRunning = false;
	Thread tempThread;
	private boolean isWaitingTmp;

	public synchronized static double opTemp(int number, int operation,
			double value) {
		switch (operation) {
		case READ:
			return temperature[number];
		case WRITE:
			temperature[number] = value;
		}
		return value;
	}

	public void runCode(String code) {
		// preprocess replace all import [file] with the content of the file
		String[] lines = code.trim().split("\n");
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < lines.length; i++) {
			if (lines[i].trim().startsWith("import"))
				try {
					sb.append(Lib.readFile(Configure.getCurrentFileParent()
							+ File.separator + lines[i].trim().split(" ")[1])
							+ "\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			else {
				sb.append(lines[i] + "\n");
			}
		}
		this.code = sb.toString();
		if (!isRunning) {
			isRunning = true;
			if (MainFrame.hasTempSerial) {
				tempThread = new Thread(new Runnable() {
					public void run() {
						int errCount = 0;
						while (isRunning) {
							try {
								getTemp(1);
								getTemp(2);
								getTemp(3);
							} catch (Exception e) {
								addUpdate(e.getMessage());
								errCount++;
								if (errCount > 10) {
									abort();
								}
							}
						}
					}

					private void getTemp(int tempNumber) throws Exception {
						double result;
						byte[] ba = { (byte) 129, (byte) 129, 82, 0, 0, 0, 83,
								0 };
						ba[0] += (byte) (tempNumber - 1);
						ba[1] += (byte) (tempNumber - 1);
						ba[6] += (byte) (tempNumber - 1);
						MainFrame.tempOut.write(ba);
						Thread.sleep(300);
						if (MainFrame.tempIn.available() != 0) {
							int[] ia = new int[10];
							for (int i = 0; i < 10; i++) {
								if (MainFrame.tempIn.available() > 0)
									ia[i] = MainFrame.tempIn.read();
								else
									throw new Exception("read failed");
							}
							if (ia[8] + ia[9] * 256 == (ia[0] + ia[1] * 256
									+ ia[2] + ia[3] * 256 + ia[4] + ia[5] * 256
									+ ia[6] + ia[7] * 256 + tempNumber)
									% (256 * 256))// 返回校验码：PV+SV+（报警状态*256+MV）+参数值+ADDR
													// 按整数加法相加后得到的余数
							{
								result = (double) (ia[0] + ia[1] * 256) / 10;
								opTemp(tempNumber - 1, Run.WRITE, result);
							} else {
								throw new Exception("compare failed");
							}
						}
						return;
					}
				});

				this.tempThread.start();
			}
			new Thread(this).start();
		} else
			return;
	}

	public void run() {
		statusSb = new StringBuffer();
		loopTimes = new Stack<Integer>();
		loopStart = new Stack<Integer>();
		String[] lines = code.trim().split("\n");
		int lineNumber = 0;
		addUpdate("// Start");
		while ((lineNumber < lines.length) && isRunning) {
			if (lines[lineNumber].equals("")) {
				lineNumber++;
				continue;
			}
			String[] words = lines[lineNumber].trim().split(" ");
			if (lines[lineNumber].startsWith("//")) {
				addUpdate(lines[lineNumber]);
				lineNumber++;
				continue;
			}
			if (words[0].equals("process")) {
				addUpdate(lines[lineNumber]);
				int processNumber = Integer.parseInt(words[1]);
				if (processNumber == 1) {
					double temp1 = Double.parseDouble(words[2]), temp2 = Double
							.parseDouble(words[3]), temp3 = Double
							.parseDouble(words[4]);
					int p1 = Integer.parseInt(words[5]);
					int p2 = Integer.parseInt(words[6]);
					while ((opTemp(2, Run.READ, 0) < temp3) && (isRunning)) {
						if (opTemp(0, Run.READ, 0) < temp1)
							openPort(p1);
						else
							closePort(p1);
						if (opTemp(1, Run.READ, 0) < temp2)
							openPort(p2);
						else
							closePort(p2);
						try {
							Thread.sleep(500);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					closePort(p1);
					closePort(p2);
				} else if (processNumber == 2) {
					double temp1 = Double.parseDouble(words[2]), temp2 = Double
							.parseDouble(words[3]), temp3 = Double
							.parseDouble(words[4]), temp4 = Double
							.parseDouble(words[7]);
					int p1 = Integer.parseInt(words[5]);
					int p2 = Integer.parseInt(words[6]);
					while ((opTemp(2, Run.READ, 0) < temp3) && (isRunning)) {
						if (opTemp(0, Run.READ, 0) < temp1)
							openPort(p1);
						else
							closePort(p1);
						if (opTemp(1, Run.READ, 0) < temp2)
							openPort(p2);
						else
							closePort(p2);
						try {
							Thread.sleep(500);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					isWaitingTmp = true;
					Timer t = new Timer();// 实例化Timer类，设置间隔时间为10000毫秒；
					t.schedule(new TimerTask() {
						@Override
						public void run() {
							isWaitingTmp = false;
						}
					}, (long) (temp4 * 1000));
					while (isWaitingTmp && isRunning) {
						if (opTemp(0, Run.READ, 0) < temp1)
							openPort(p1);
						else
							closePort(p1);
						if (opTemp(1, Run.READ, 0) < temp2)
							openPort(p2);
						else
							closePort(p2);
						try {
							Thread.sleep(500);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					closePort(p1);
					closePort(p2);
				} else if (processNumber == 3) {
					double temp1 = Double.parseDouble(words[2]), temp2 = Double
							.parseDouble(words[3]), temp3 = Double
							.parseDouble(words[6]);
					int p1 = Integer.parseInt(words[4]);
					int p2 = Integer.parseInt(words[5]);
					isWaitingTmp = true;
					Timer t = new Timer();// 实例化Timer类，设置间隔时间为10000毫秒；
					t.schedule(new TimerTask() {
						@Override
						public void run() {
							isWaitingTmp = false;
						}
					}, (long) (temp3 * 1000));
					while (isWaitingTmp && isRunning) {
						if (opTemp(0, Run.READ, 0) < temp1)
							openPort(p1);
						else
							closePort(p1);
						if (opTemp(1, Run.READ, 0) < temp2)
							openPort(p2);
						else
							closePort(p2);
						try {
							Thread.sleep(500);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					closePort(p1);
					closePort(p2);
				} else if (processNumber == 4) {
					double temp1 = Double.parseDouble(words[2]);
					while (isRunning) {
						if (opTemp(2, Run.READ, 0) < temp1) {
							try {
								Thread.sleep(500);
							} catch (Exception e) {
								e.printStackTrace();
							}
							if (opTemp(2, Run.READ, 0) < temp1)
								break;
						}
						try {
							Thread.sleep(500);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				lineNumber++;
				continue;
			}
			switch (words.length) {
			case 1:
				if (words[0].equals("wait")) {
					isWaiting = true;
					addUpdate("waiting, click 'Resume' to resume");
					while (isWaiting) {
						try {
							Thread.currentThread();
							Thread.sleep(100);
						} catch (InterruptedException e) {
							//
							e.printStackTrace();
						}
					}
				} else if (words[0].equals("}")) {
					if (loopTimes.peek() != 0) {
						lineNumber = loopStart.peek();
						loopTimes.push(loopTimes.pop() - 1);
					} else {
						loopStart.pop();
						loopTimes.pop();
					}
				}
				break;
			case 2:
				if (words[0].equals("open")) {
					String[] ports = words[1].split(",");
					for (int i = 0; i < ports.length; i++) {
						openPort(Integer.parseInt(ports[i]));
						addUpdate("open " + ports[i]);
					}
				} else if (words[0].equals("close")) {
					String[] ports = words[1].split(",");
					for (int i = 0; i < ports.length; i++) {
						closePort(Integer.parseInt(ports[i]));
						addUpdate("close " + ports[i]);
					}
				} else if (words[0].equals("wait")) {
					addUpdate("wait for " + words[1] + " seconds");
					try {
						Thread.currentThread();
						Thread.sleep((int) (Double.parseDouble(words[1]) * 1000));
					} catch (NumberFormatException e) {
						//
						e.printStackTrace();
					} catch (InterruptedException e) {
						//
						e.printStackTrace();
					}
				}
				break;
			case 3:
				if (words[0].equals("loop")) {
					loopStart.push(lineNumber);
					loopTimes.push(Integer.parseInt(words[1]) - 1);
				} else if (words[0].equals("setTemp")) {
					addUpdate(lines[lineNumber]);
					try {
						setTemp(Integer.parseInt(words[1]),
								Double.parseDouble(words[2]));
					} catch (NumberFormatException e) {
						//
						e.printStackTrace();
					} catch (IOException e) {
						//
						e.printStackTrace();
					} catch (InterruptedException e) {
						//
						e.printStackTrace();
					}
				} else if (words[0].equals("waitT3Until")) {
					addUpdate(lines[lineNumber]);
					switch (words[1].charAt(0)) {
					case '<':
						while (Double.parseDouble(words[2]) < opTemp(2,
								Run.READ, 0))
							try {
								Thread.currentThread();
								Thread.sleep(500);
							} catch (InterruptedException e1) {
								//
								e1.printStackTrace();
							}
						break;
					case '>':
						while (Double.parseDouble(words[2]) > opTemp(2,
								Run.READ, 0))
							try {
								Thread.currentThread();
								Thread.sleep(500);
							} catch (InterruptedException e) {
								//
								e.printStackTrace();
							}
						break;
					}
				}
				break;
			}
			lineNumber++;
		}
		addUpdate("end");
		try {
			Thread.currentThread();
			Thread.sleep(500);
		} catch (InterruptedException e) {
			//
			e.printStackTrace();
		}
		Run.abort();
		opTemp(0, Run.WRITE, -2);
		opTemp(1, Run.WRITE, -2);
		opTemp(2, Run.WRITE, -2);
	}

	private void setTemp(int n, double setT) throws IOException,
			InterruptedException {

		double result;
		byte checkSumLow, checkSumHigh;
		checkSumHigh = (byte) ((67 + n + setT * 10) / 256);
		checkSumLow = (byte) ((67 + n + setT * 10) % 256);
		byte[] ba = { (byte) 129, (byte) 129, 67, 0, (byte) (setT * 10 % 256),
				(byte) (setT * 10 / 256), checkSumLow, checkSumHigh };
		ba[0] += (byte) (n - 1);
		ba[1] += (byte) (n - 1);
		ba[6] += (byte) (n - 1);
		for (int i = 0; i < 8; i++) {
			if (ba[i] < 0)
				System.out.printf("%d ", ba[i] + 256);
			else
				System.out.printf("%d ", ba[i]);
		}
		System.out.println();
		if (MainFrame.hasTempSerial) {
			for (int j = 0; j < 2; j++) {
				MainFrame.tempOut.write(ba, 0, ba.length);
				Thread.currentThread();
				Thread.sleep(300);
				if (MainFrame.tempIn.available() != 0) {
					int[] ia = new int[10];
					for (int i = 0; i < 10; i++) {
						if (MainFrame.tempIn.available() > 0)
							ia[i] = MainFrame.tempIn.read();
						else
							return;
					}
					if (ia[8] + ia[9] * 256 == (ia[0] + ia[1] * 256 + ia[2]
							+ ia[3] * 256 + ia[4] + ia[5] * 256 + ia[6] + ia[7]
							* 256 + n)
							% (256 * 256))// 返回校验码：PV+SV+（报警状态*256+MV）+参数值+ADDR
											// 按整数加法相加后得到的余数
					{
						result = (double) (ia[0] + ia[1] * 256) / 10;
						temperature[n - 1] = result;
						return;
					} else {
						addUpdate("//checksum is wrong, try again");
					}
				}
				addUpdate("//setTemp failed, so abort:" + n);
				Run.abort();
			}
		}
	}

	public static void closePort(int p) {
		if (MainFrame.debugLevel == 0)
			System.out.println("close " + p);
		if (MainFrame.hasSerial) {
			String s;
			if (p < 10)
				s = "O(00,00" + p + ",0)";
			else
				s = "O(00,0" + p + ",0)";
			char[] ca = s.toCharArray();
			byte[] ba = new byte[ca.length];
			for (int i = 0; i < ca.length; i++) {
				ba[i] = (byte) ca[i];
			}
			try {
				MainFrame.fortyPortsOut.write(ba, 0, ba.length);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void openPort(int p) {
		if (MainFrame.debugLevel == 0)
			System.out.println("open " + p);
		if (MainFrame.hasSerial) {
			String s;
			if (p < 10)
				s = "O(00,00" + p + ",1)";
			else
				s = "O(00,0" + p + ",1)";
			char[] ca = s.toCharArray();
			byte[] ba = new byte[ca.length];
			for (int i = 0; i < ca.length; i++) {
				ba[i] = (byte) ca[i];
			}
			try {
				MainFrame.fortyPortsOut.write(ba, 0, ba.length);
			} catch (IOException e) {
				//
				e.printStackTrace();
			}
		}
	}

	private static void addUpdate(String string) {
//		if (Configure.runNoisy || string.startsWith("//"))
			statusSb.append(string + "\n");
	}

	public static void abort() {
		if (isRunning) {
			closeAll();
			isWaiting = false;
			isRunning = false;
			addUpdate("//abort");
			MainFrame.rf.recordTimer.cancel();
			MainFrame.rf.drawLineGraph.saveGraph();
			Lib.closeCOMMs();
		}
	}

	private static void closeAll() {
		for (int i = 1; i < 41; i++) {
			closePort(i);
		}
	}

}
