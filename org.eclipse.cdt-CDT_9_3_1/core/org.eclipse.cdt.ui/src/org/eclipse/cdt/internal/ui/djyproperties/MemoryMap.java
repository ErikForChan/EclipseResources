package org.eclipse.cdt.internal.ui.djyproperties;

public class MemoryMap {

	private String flashStart;
	private String flashSize;
	private String ramStart;
	private String ramSize;
	private String extromStart;
	private String extromSize;
	private String extramStart;
	private String extramSize;
	private String ibootSize;
	
	public String getFlashStart() {
		return flashStart;
	}
	public void setFlashStart(String flashStart) {
		this.flashStart = flashStart;
	}
	public String getFlashSize() {
		return flashSize;
	}
	public void setFlashSize(String flashSize) {
		this.flashSize = flashSize;
	}
	public String getRamStart() {
		return ramStart;
	}
	public void setRamStart(String ramStart) {
		this.ramStart = ramStart;
	}
	public String getRamSize() {
		return ramSize;
	}
	public void setRamSize(String ramSize) {
		this.ramSize = ramSize;
	}
	public String getExtromStart() {
		return extromStart;
	}
	public void setExtromStart(String extromStart) {
		this.extromStart = extromStart;
	}
	public String getExtromSize() {
		return extromSize;
	}
	public void setExtromSize(String extromSize) {
		this.extromSize = extromSize;
	}
	public String getExtramStart() {
		return extramStart;
	}
	public void setExtramStart(String extramStart) {
		this.extramStart = extramStart;
	}
	public String getExtramSize() {
		return extramSize;
	}
	public void setExtramSize(String extramSize) {
		this.extramSize = extramSize;
	}
	public String getIbootSize() {
		return ibootSize;
	}
	public void setIbootSize(String ibootSize) {
		this.ibootSize = ibootSize;
	}
	public MemoryMap(String flashStart, String flashSize, String ramStart, String ramSize,
			String extromStart, String extromSize, String extramStart, String extramSize, String ibootSize) {
		super();
		this.flashStart = flashStart;
		this.flashSize = flashSize;
		this.ramStart = ramStart;
		this.ramSize = ramSize;
		this.extromStart = extromStart;
		this.extromSize = extromSize;
		this.extramStart = extramStart;
		this.extramSize = extramSize;
		this.ibootSize = ibootSize;
	}
	
}
