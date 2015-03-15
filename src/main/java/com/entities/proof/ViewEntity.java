package com.entities.proof;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name = "view_entity")
public class ViewEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer id;

	private String viewData;
	
	private String processedViewData;
	private long version;
	
	private boolean dirty;

	ViewEntity() {
		// default constructor
	}

	public ViewEntity(int id) {
		this.id = id;
	}

	@Id
	@Column(name = "view_id")
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
	@Column(name = "processed_view_data")
	public String getProcessedViewData() {
		System.out.println("dirty? " + dirty + " ...getting view data: " + viewData + ", processed: " + processedViewData);
		if(dirty) {
			// comment this line out to get the test to pass!
			dirty = false;
			// It's also the correct thing to do:
			//  - if an entity has been pulled from the database, assume it's been modified
			//  - can't do performance enhancement at the sake of correctness
			processedViewData = processData(viewData);
		}
		return processedViewData;
	}
	
	private String processData(String viewData) {
		return viewData.toUpperCase();
	}

	public void setProcessedViewData(String processedViewData) {
		this.processedViewData = processedViewData;
	}

	public void setViewData(String viewData) {
		this.viewData = viewData;
	}
	
	public void markDirty() {
		dirty = true;
	}

	@Version
	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return "ViewEntity [id=" + id + ", viewData=" + viewData
				+ ", processedViewData=" + processedViewData + ", version=" + version
				+ ", dirty=" + dirty + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((getProcessedViewData() == null) ? 0 : getProcessedViewData().hashCode());
		result = prime * result + (dirty ? 1231 : 1237);
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + (int) (version ^ (version >>> 32));
		result = prime * result
				+ ((viewData == null) ? 0 : viewData.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ViewEntity other = (ViewEntity) obj;
		if (getProcessedViewData() == null) {
			if (other.getProcessedViewData() != null)
				return false;
		} else if (!getProcessedViewData().equals(other.getProcessedViewData()))
			return false;
		if (dirty != other.dirty)
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (version != other.version)
			return false;
		if (viewData == null) {
			if (other.viewData != null)
				return false;
		} else if (!viewData.equals(other.viewData))
			return false;
		return true;
	}
}