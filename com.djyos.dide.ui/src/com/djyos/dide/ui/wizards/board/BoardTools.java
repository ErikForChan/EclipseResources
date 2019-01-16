package com.djyos.dide.ui.wizards.board;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;

import com.djyos.dide.ui.DPluginImages;
import com.djyos.dide.ui.helper.DideHelper;
import com.djyos.dide.ui.objects.Board;
import com.djyos.dide.ui.objects.Core;
import com.djyos.dide.ui.objects.CoreMemory;
import com.djyos.dide.ui.objects.Cpu;
import com.djyos.dide.ui.objects.OnBoardCpu;
import com.djyos.dide.ui.objects.OnBoardMemory;
import com.djyos.dide.ui.wizards.djyosProject.tools.UnitData;

public class BoardTools {

	protected static void Display_BoardDetails(TreeItem item, List<Board> boardsList, List<Cpu> allCpus, Text configInfoText) {
		// TODO Auto-generated method stub
		String boardDesc = "";
		if (item != null) {
			String selectText = item.getText();
			if (selectText.contains("���")) {
				boardDesc += "ѡ�а��������ʾѡ�еİ��������Ϣ";
			} else {
				String type = item.getData("type").toString();
				
				for (int i = 0; i < boardsList.size(); i++) {
					Board board = boardsList.get(i);
					if (type.equals("board")) {
						if (board.getBoardName().equals(selectText)) {
							List<OnBoardCpu> cpus = board.getOnBoardCpus();
							List<OnBoardMemory> sharedMems = board.getShare_memorys();
							boardDesc += "����Cpu��";
							for(OnBoardCpu onCpu:cpus ) {
								boardDesc += "\n\t"+onCpu.getCpuName();
							}
							if(sharedMems.size() > 0) {
								boardDesc += "\n���ع���洢��";
								for (int k = 0; k < sharedMems.size(); k++) {
									boardDesc += "\n\t�ڴ�" + (k + 1) + "��\n\t";
									if (sharedMems.get(k).getType() != null) {
										boardDesc += sharedMems.get(k).getType();
									}
									if (sharedMems.get(k).getStartAddr() != null) {
										boardDesc += "����ʼ��ַ��" + sharedMems.get(k).getStartAddr();
									}
									if (sharedMems.get(k).getSize() != null) {
										boardDesc += "����С��" + sharedMems.get(k).getSize();
									}
								}
							}
							break;
						}
					} else if (type.equals("cpu") || type.equals("core")) {
						if (item.getParentItem().getText().equals(board.getBoardName())
								|| item.getParentItem().getParentItem().getText().equals(board.getBoardName())) {
							List<OnBoardCpu> onBoardCpus = board.getOnBoardCpus();
							for (OnBoardCpu o : onBoardCpus) {
								Cpu myCpu = DideHelper.getCpuByonBoard(o, allCpus);
								if (type.equals("core")) {
									if (myCpu.getCpuName().equals(item.getParentItem().getText())) {
										for (int c = 0; c < myCpu.getCores().size(); c++) {
											Core core = myCpu.getCores().get(c);
											String coreName = core.getName()!=null?core.getName():("Core" + (c + 1));
											if (selectText.equals(coreName)) {
												if (core.getArch().getSerie() != null) {
													boardDesc += "�ܹ���\n" + core.getArch().getSerie();
												}
												if (core.getArch().getMarch() != null) {
													boardDesc += "��" + core.getArch().getMarch();
												}
												if (core.getArch().getMcpu() != null) {
													boardDesc += "��" + core.getArch().getMcpu();
												}
												if (core.getFpuType() != null) {
													boardDesc += "\n���㣺" + core.getFpuType();
												}
												if (core.getResetAddr() != null) {
													boardDesc += "\n��λ��ַ��" + core.getResetAddr();
												}
												if (core.getMemorys().size() != 0) {
													List<CoreMemory> memorys = core.getMemorys();
													for (int k = 0; k < memorys.size(); k++) {
														boardDesc += "\n�ڴ�" + (k + 1) + "��\n";
														if (memorys.get(k).getType() != null) {
															boardDesc += memorys.get(k).getType();
														}
														if (memorys.get(k).getStartAddr() != null) {
															boardDesc += "����ʼ��ַ��" + memorys.get(k).getStartAddr();
														}
														if (memorys.get(k).getSize() != null) {
															boardDesc += "����С��" + memorys.get(k).getSize();
														}
													}
												}
											}
										}
									}
								} else {
									if (o.getCpuName().equals(selectText)) {
										String chipString = "";
										for (int k = 0; k < o.getChips().size(); k++) {
											chipString += ((k != 0 ? "��" : "") + o.getChips().get(k).getChipName());
										}

										String peripheralString = "";
										for (int k = 0; k < o.getPeripherals().size(); k++) {//((k != 0 ? "��" : "")
											peripheralString += "\n\t" + o.getPeripherals().get(k).getName();
										}
										boardDesc += "�ں˸���" + "�� " + myCpu.getCores().size();
										boardDesc += "\n������Ƶ��: " + UnitData.UnitMhz(String.valueOf(o.getMianClk()))+
												"\nRtc��Ƶ��: " + o.getRtcClk() + 
												"\nоƬ: " + chipString + 
												"\n����: " + peripheralString + "\n\n";
									}
								}

							}
						}
					}
				}
			}
			configInfoText.setText(boardDesc);
		}
	}

	// ���Ŀ¼����Ӱ���Cpu
	public static void fillBoardChilds(Board board, TreeItem t, List<Cpu> allCpus) {
		// TODO Auto-generated method stub
		List<OnBoardCpu> onBoardCpus = board.getOnBoardCpus();
		for (OnBoardCpu o : onBoardCpus) {
			TreeItem cpuItem = new TreeItem(t, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
//			System.out.println("onBoardCpu��  "+o.getCpuName());
			fillCpuChilds(DideHelper.getCpuByonBoard(o, allCpus), cpuItem);
			cpuItem.setText(o.getCpuName());
			cpuItem.setData(o.getCpuName());
			cpuItem.setData("type", "cpu");
			cpuItem.setImage(DPluginImages.OBJ_CPU_VIEW.createImage());
		}
	}

	// ����CpuĿ¼������ں�
	private static void fillCpuChilds(Cpu c, TreeItem cpuItem) {
		// TODO Auto-generated method stub
//		System.out.println("cpuName:   "+c.getCpuName());
		List<Core> cores = c.getCores();
		for (int i = 0; i < cores.size(); i++) {
			TreeItem coreItem = new TreeItem(cpuItem, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
			String coreName = DideHelper.getCoreName(cores.get(i),i);
			coreItem.setText(coreName);
			coreItem.setData(coreName);
			coreItem.setData("type", "core");
			coreItem.setImage(DPluginImages.OBJ_CORE_VIEW.createImage());
		}
	}
}
