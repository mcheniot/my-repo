package com.telus.apiprogram.poc.bigquery;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.bigquery.Acl;
import com.google.cloud.bigquery.Acl.Role;
import com.google.cloud.bigquery.Acl.User;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQuery.DatasetListOption;
import com.google.cloud.bigquery.BigQuery.TableListOption;
import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.DatasetId;
import com.google.cloud.bigquery.DatasetInfo;
import com.google.cloud.bigquery.FieldValueList;
import com.google.cloud.bigquery.FormatOptions;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.JobId;
import com.google.cloud.bigquery.JobInfo;
import com.google.cloud.bigquery.JobStatistics.LoadStatistics;
import com.google.cloud.bigquery.LoadJobConfiguration;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardTableDefinition;
import com.google.cloud.bigquery.Table;
import com.google.cloud.bigquery.TableDataWriteChannel;
import com.google.cloud.bigquery.TableDefinition;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.bigquery.TableInfo;
import com.google.cloud.bigquery.TableResult;
import com.google.cloud.bigquery.WriteChannelConfiguration;

public class BigQuerySample {

	static final String datasetName = "iotcharges";

	static final String projectId = "alert-cursor-295918";

	static final String tableName = "iotinvoice";

	private static void runQuery(BigQuery bigquery) throws Exception {

		QueryJobConfiguration queryConfig = QueryJobConfiguration
				.newBuilder("SELECT commit, author, repo_name " + "FROM `bigquery-public-data.github_repos.commits` "
						+ "WHERE subject like '%bigquery%' " + "ORDER BY subject DESC LIMIT 10")
				// Use standard SQL syntax for queries.
				// See: https://cloud.google.com/bigquery/sql-reference/
				.setUseLegacySql(false).build();

		// Create a job ID so that we can safely retry.
		JobId jobId = JobId.of(UUID.randomUUID().toString());
		Job queryJob = bigquery.create(JobInfo.newBuilder(queryConfig).setJobId(jobId).build());

		// Wait for the query to complete.
		queryJob = queryJob.waitFor();

		// Check for errors
		if (queryJob == null) {
			throw new RuntimeException("Job no longer exists");
		} else if (queryJob.getStatus().getError() != null) {
			// You can also look at queryJob.getStatus().getExecutionErrors() for all
			// errors, not just the latest one.
			throw new RuntimeException(queryJob.getStatus().getError().toString());
		}

		// Get the results.
		TableResult result = queryJob.getQueryResults();

		// Print all pages of the results.
		for (FieldValueList row : result.iterateAll()) {
			// String type
			String commit = row.get("commit").getStringValue();
			// Record type
			FieldValueList author = row.get("author").getRecordValue();
			String name = author.get("name").getStringValue();
			String email = author.get("email").getStringValue();
			// String Repeated type
			String repoName = row.get("repo_name").getRecordValue().get(0).getStringValue();
			System.out.printf("Repo name: %s Author name: %s email: %s commit: %s\n", repoName, name, email, commit);
		}

	}

	private static void createDataset(BigQuery bigquery) {
		try {

			// Initialize client that will be used to send requests. This client only needs
			// to be created
			// once, and can be reused for multiple requests.

			DatasetInfo datasetInfo = DatasetInfo.newBuilder(datasetName).build();

			Dataset newDataset = bigquery.create(datasetInfo);
			String newDatasetName = newDataset.getDatasetId().getDataset();
			System.out.println(newDatasetName + " created successfully");
		} catch (BigQueryException e) {
			System.out.println("Dataset was not created. \n" + e.toString());
		}
	}

	private static void updateDatasetAccess(BigQuery bigquery) {
		try {
			// Initialize client that will be used to send requests. This client only needs
			// to be created
			// once, and can be reused for multiple requests.

			Acl newEntry = Acl.of(new User("yunqiang.chen@gmail.com"), Role.READER);

			Dataset dataset = bigquery.getDataset(datasetName);

			// Get a copy of the ACLs list from the dataset and append the new entry
			ArrayList<Acl> acls = new ArrayList<>(dataset.getAcl());
			acls.add(newEntry);

			bigquery.update(dataset.toBuilder().setAcl(acls).build());
			System.out.println("Dataset Access Control updated successfully");

		} catch (BigQueryException e) {
			System.out.println("Dataset Access control was not updated \n" + e.toString());
		}
	}

	public static void listDatasets(BigQuery bigquery) {

		try {
			// Initialize client that will be used to send requests. This client only needs
			// to be created
			// once, and can be reused for multiple requests.
			Page<Dataset> datasets = bigquery.listDatasets(projectId, DatasetListOption.pageSize(100));

			if (datasets == null) {
				System.out.println("Dataset does not contain any models");
				return;
			}
			datasets.iterateAll()
					.forEach(dataset -> System.out.printf("Success! Dataset ID: %s \n", dataset.getDatasetId()));

		} catch (BigQueryException e) {
			System.out.println("Project does not contain any datasets \n" + e.toString());
		}
	}

	public static void getDatasetInfo(BigQuery bigquery) {
		try {
			// Initialize client that will be used to send requests. This client only needs
			// to be created
			// once, and can be reused for multiple requests.
			DatasetId datasetId = DatasetId.of(projectId, datasetName);
			Dataset dataset = bigquery.getDataset(datasetId);

			// View dataset properties
			String description = dataset.getDescription();
			System.out.println(description);

			// View tables in the dataset
			// For more information on listing tables see:
			// https://javadoc.io/static/com.google.cloud/google-cloud-bigquery/0.22.0-beta/com/google/cloud/bigquery/BigQuery.html
			Page<Table> tables = bigquery.listTables(datasetName, TableListOption.pageSize(100));

			tables.iterateAll().forEach(table -> System.out.print(table.getTableId().getTable() + "\n"));

			System.out.println("Dataset info retrieved successfully.");
		} catch (BigQueryException e) {
			System.out.println("Dataset info not retrieved. \n" + e.toString());
		}
	}

	public static long writeFileToTable(BigQuery bigquery) throws IOException, InterruptedException, TimeoutException {
		// [START bigquery_load_from_file]
		TableId tableId = TableId.of(datasetName, "iotinvoice");
		WriteChannelConfiguration writeChannelConfiguration = WriteChannelConfiguration.newBuilder(tableId)
				.setFormatOptions(FormatOptions.csv()).build();
		// The location must be specified; other fields can be auto-detected.
		JobId jobId = JobId.newBuilder().setLocation("US").build();
		TableDataWriteChannel writer = bigquery.writer(jobId, writeChannelConfiguration);
		// Write data to writer
		try (OutputStream stream = Channels.newOutputStream(writer)) {
			Path csvPath = new File("C:\\projects\\IoT-M2M\\2020-charges.csv").toPath();

			Files.copy(csvPath, stream);
		}

		// Get load job
		Job job = writer.getJob();
		job = job.waitFor();
		LoadStatistics stats = job.getStatistics();
		return stats.getOutputRows();
		// [END bigquery_load_from_file]
	}

	public static void LoadCSVFromGCSAutodetect(BigQuery bigquery) {
		try {
			// Initialize client that will be used to send requests. This client only needs
			// to be created
			// once, and can be reused for multiple requests.

			TableId tableId = TableId.of(datasetName, tableName);
			LoadJobConfiguration loadConfig = LoadJobConfiguration
					.newBuilder(tableId, "gs://bucket-iot-charges-invoice/2020-charges.csv")
					.setFormatOptions(FormatOptions.csv()).setAutodetect(true).build();

			// Load data from a GCS JSON file into the table
			Job job = bigquery.create(JobInfo.of(loadConfig));
			// Blocks until this load table job completes its execution, either failing or
			// succeeding.
			job = job.waitFor();
			if (job.isDone()) {
				System.out.println("Json Autodetect from GCS successfully loaded in a table");
			} else {
				System.out.println(
						"BigQuery was unable to load into the table due to an error:" + job.getStatus().getError());
			}
		} catch (BigQueryException | InterruptedException e) {
			System.out.println("Column not added during load append \n" + e.toString());
		}
	}

	public static void createTableWithoutSchema(BigQuery bigquery) {
		try {
			// Initialize client that will be used to send requests. This client only needs
			// to be created
			// once, and can be reused for multiple requests.

			TableId tableId = TableId.of(datasetName, tableName);
			TableDefinition tableDefinition = StandardTableDefinition.of(Schema.of());
			TableInfo tableInfo = TableInfo.newBuilder(tableId, tableDefinition).build();

			bigquery.create(tableInfo);
			System.out.println("Table created successfully");
		} catch (BigQueryException e) {
			System.out.println("Table was not created. \n" + e.toString());
		}
	}

	public static void main(String... args) throws Exception {

		System.out.println("==================");
		// if proxy needed to access outside telus, please uncomment these two lines
		System.setProperty("https.proxyHost", "pac.tsl.telus.com");
		System.setProperty("https.proxyPort", "8080");

		BigQuery bigquery = BigQueryOptions.newBuilder()
				.setCredentials(
						ServiceAccountCredentials.fromStream(new FileInputStream("iotbucketserviceaccountkey.json")))
				.build().getService();

		// runQuery(bigquery);

		// createDataset(bigquery);

		// updateDatasetAccess(bigquery);

		// listDatasets(bigquery);

		// getDatasetInfo(bigquery);

		// writeFileToTable(bigquery);

		LoadCSVFromGCSAutodetect(bigquery);

		//createTableWithoutSchema(bigquery);

	}

}
