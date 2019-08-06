/*
 * Copyright 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazebit.notify.job.jpa.model;

import com.blazebit.notify.job.Job;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@MappedSuperclass
@Table(name = "job")
public abstract class AbstractJob extends BaseEntity implements Job {

	private static final long serialVersionUID = 1L;
	
	private String name;
	private JobConfiguration jobConfiguration = new JobConfiguration();
	/**
	 * The time at which the job was created
	 */
	private Instant creationTime;

	protected AbstractJob() {
	}

	protected AbstractJob(Long id) {
		super(id);
	}

	@Override
	@NotNull
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	@Embedded
	@AssociationOverrides({
			@AssociationOverride(name = "executionTimeFrames", joinTable = @JoinTable(name = "job_execution_time_frames", foreignKey = @ForeignKey(name = "job_execution_time_frames_fk_job"))),
			@AssociationOverride(name = "jobParameters", joinTable = @JoinTable(name = "job_parameter", foreignKey = @ForeignKey(name = "job_parameter_fk_job")))
	})
	public JobConfiguration getJobConfiguration() {
		return jobConfiguration;
	}

	public void setJobConfiguration(JobConfiguration jobConfiguration) {
		this.jobConfiguration = jobConfiguration;
	}

	@Override
	@Column(nullable = false)
	public Instant getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Instant creationTime) {
		this.creationTime = creationTime;
	}

	@PrePersist
	protected void onPersist() {
		if (this.creationTime == null) {
			this.creationTime = Instant.now();
		}
	}
}
