package org.talend.cloudera.navigator.api;

import java.util.ArrayList;
import java.util.List;

import com.cloudera.nav.sdk.model.SourceType;
import com.cloudera.nav.sdk.model.annotations.MClass;
import com.cloudera.nav.sdk.model.annotations.MRelation;
import com.cloudera.nav.sdk.model.entities.EndPointProxy;
import com.cloudera.nav.sdk.model.entities.EntityType;
import com.cloudera.nav.sdk.model.relations.RelationRole;

@MClass(model = "talend")
public class TalendInputEntity extends TalendEntity {
	
	private List<String> nextEntitiesId;
	
	@MRelation(role = RelationRole.TARGET)
	private List<EndPointProxy> targetProxies;

	public TalendInputEntity(String namespace, String jobId, String componentName) {
		super(namespace, jobId, componentName);
		targetProxies = new ArrayList<EndPointProxy>();
	    nextEntitiesId = new ArrayList<String>();
	}
	
	public void addNextEntity(String entityId){
		this.nextEntitiesId.add(entityId);
		EndPointProxy endpointProxy = new EndPointProxy(entityId, SourceType.PLUGIN, EntityType.OPERATION_EXECUTION);
		this.targetProxies.add(endpointProxy);
	}

	public List<String> getNextEntitiesId() {
		return nextEntitiesId;
	}

	public List<EndPointProxy> getTargetProxies() {
		return targetProxies;
	}
	
	@Override
	public String toString() {
		return getName() + " (" + getEntityId() + ")" + " --->" + this.nextEntitiesId;
	}

}
