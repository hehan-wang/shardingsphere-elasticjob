# build
docker build -t elastic-job-lite-console:2.1.5 .

# run
#docker run -d -p 8080:8899 elastic-job-lite-console:2.1.5

#push
docker tag elastic-job-lite-console:2.1.5 micr.cloud.mioffice.cn/elastic-job/elastic-job-lite-console:2.1.5
docker push micr.cloud.mioffice.cn/elastic-job/elastic-job-lite-console:2.1.5