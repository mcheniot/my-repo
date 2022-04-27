package com.telus.apiprogram.poc.bigquery;

import org.springframework.web.bind.annotation.RestController;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.FieldValueList;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.JobId;
import com.google.cloud.bigquery.JobInfo;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.TableResult;
import java.util.UUID;

@RestController
public class BQController {
	
	

	private BigQuery getBQClient() {

		BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();

		return bigquery;
	}
}
