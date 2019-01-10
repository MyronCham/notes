------------------------
Dockerfile				|
------------------------
	# Dockerfile����DSL(Domain Specifile Language)�﷨��ָ��������һ��Docker����
	# Docker�����ϰ�����������ִ��Dockerfile�е�����
		* Docker �ӻ�����������һ��������
		* ִ��һ��ָ��,�����������޸�
		* ִ�������� Docker Commit �Ĳ���,�ύһ���µľ����
		* Docker�ٻ��ڸղ��ύ�ľ�������һ��������
		* ִ��Dockerfile�е���һ��ָ��ֱ������ָ�ִ�����

	# ���Dockerfile����һЩԭ��û����������,���ǿ��Եõ�һ������ʹ�õľ���
		* ���Ի��ڸþ�������һ������,���뽻��ʽ�����鿴�Ų�ԭ��

	# �� # ��ͷ��ʾע��
	# ��һ��֮�������: FROM
		* ��ʾ��������,������ָ��һ���Ѿ����ڵľ���
	

------------------------
Dockerfile				|
------------------------
	FROM
		* �����ľ���
			FROM centos:7

	MAINTAINER
		* ������Ϣ
			MAINTAINER KevinBlandy "747692844@qq.com"
	ENV
		* ��������
			ENV JAVA_HOME /usr/local/java
		* Ҳ����һ�������ö��
			ENV JAVA_HOM=/home/java PYTHON_HOME=/home/python
		* Ҳ������������ָ�������øñ���
			ENV DIR /opt/app
			WORKDIR $DIR
		* ��Щ���ڱ�����־ñ��浽���񴴽�������,�����ù� env ָ��鿴

	RUN 
		* ����ʱ��,ִ�е�shell����
			RUN yum -y install git

	EXPOSE
		* ���⹫���Ķ˿�
			EXPOSE 80
	
	CMD
		* ��������ʱ���е�����
			COM ["/bin/true","-l"]
		* �� docker run ָ���ָ��������һ����,���Ҹ�ָ��ָ���������Ḳ����
		* �����ļ���ֻ�ܶ���һ��CMDָ��,��������˶��,��ôֻ�����һ����Ч
	
	ENTRYPOINT
		* ������ CMD ָ��,Ҳ������ִ�������
			ENTRYPOINT ["/usr/sbin/nginx"]
		* �����ᱻ docker run ָ��������
		* docker run ָ��ĵ������������Ϊֵ,���ݸ��������
			ENTRYPOINT ["/usr/sbin/nginx"]
				+
			docker run ... nginx-image -g "daemon off"
				=
			/usr/sbin/nginx -g "daemon off"
		
		* ʵ����Ҫ���Ǹ�����,����ͨ�� --entrypoint ������
		* �������CMDʹ������һ��ָ��
			ENTRYPOINT ["/usr/sbin/nginx"]
				+
			CMD ["-h"]
				=
			/usr/sbin/nginx -h
		

	WORKDIR
		* �ڴӾ��񴴽�һ���µ�����ʱ,���������ڲ�����һ������Ŀ¼
			WORKDIR /usr/local/webapp
		* CMD �� ENTRYPOINT ָ��������Ŀ¼��ִ��
		* ��������Գ��ֶ��,��ʾĿ¼�л�
			WORKDIR /usr/local/db
			RUN mysql install
			WORKDIR /usr/local/webapp
			ENTRYPOINT ["setup"]
		*  ������docker runʱͨ�� -w ���������Ǹ�����
			docer run ... -w /var/log ...
		

	USER
		* ָ��������ĸ��û�ȥִ��
			USER root
		* ����ָ���û���,uid,�û���,gid,�Լ��������
			USER user
			USER user:group
		
		* ���δָ��,Ĭ��Ϊ root
	
	VOLUME
		* ���ھ��񴴽�������,���Ӿ�
		* һ�������Դ���һ�����߶���������ض���Ŀ¼,���Ŀ¼�����ƹ������ļ�ϵͳ,�����ṩ���¹������ݻ��߶����ݽ��г־û��Ĺ���
			* ������������֮�乲��������
			* �Ծ����޸���������Ч��
			* �Ծ����޸�,����Ը��¾������Ӱ��
			* ����һֱ����,ֱ��û������ʹ����
		* ����һ����ָ��һ�����߶����
			VOLUME ["/opt/project","/data"]
		
	ADD
		* �ѹ��������µ��ļ���Ŀ¼���Ƶ�������
		* ��Ҫָ��Դ�ļ���Ŀ���ļ���λ��
			ADD software.lic /opt/application/software.lic
		* Դ�ļ�Ҳ������һ��url,���ǲ��ܶԹ���Ŀ¼����ļ����в���
		* ͨ����β���ж����ļ������ļ���,�����β�� / �ַ���β,��ʾΪһ���ļ���,��֮��Ϊһ���ļ�
		* ���Դ�ļ���ѹ���ļ�(zip),��Ŀ����һ��Ŀ¼,���������ѹ
			ADD a.zip /usrl/local/a/
		
		* ��ָ����ù�������ʧЧ,���ͨ��ADDָ��������һ���ļ�����Ŀ¼,��ôDf�ļ��еĺ���ָ��ܼ���ʹ��֮ǰ�Ļ���
	COPY
		* ��ADDһ��,Ψһ��ͬ��ʱ��,���cp��ʱ��ѹ���ļ�,������н�ѹ����


	LABEL
		*  ΪDocker��������Ԫ������Ϣ,k=v��ʽչʾ
			LABEL version="10.1"
			LABEL location="�й� - ����" type="DLK" role="ADMIN"
		* ����ͨ�� docker inspect ���鿴 Docker �����еı�ǩ��Ϣ
	
	STOPSIGNAL
		* ֹͣ������ʱ��,����ָ����ϵͳ�źŸ�����
		* �źű������ں�ϵͳ�����еĺϷ���(���ֻ�������)
			STOPSIGNAL 9 
			STOPSIGNAL SIGKILL
		
	ARG
		* ������docker build��������ʱ�򴫵ݹ�������ʱ�ı���
		* �ڹ���ʱʹ�� --build-arg,�û�ֻ���ڹ���ʱָ��Df�ļ��ж�����Ĳ���
			ARG build
			docker build ... --build-arg build=1234 ....

		* Ҳ������Ĭ��ֵ,���buildʱûָ��,��ʹ��Ĭ��ֵ
			ARG webapp_usr=user
		
		* DockerԤ������N��ARG����,����ֱ�Ӹ�ֵʹ��
			HTTP_PROXY
	
	ONBUILD
		* Ϊ�������Ӵ�����,���þ����������������������ʱ��,�þ����еĴ������ᱻִ��
		* ���������ڹ��������в�����ָ��,��Щָ���Ǹ���FROM֮��ִ�е�(FROM �ĸ�����,�ĸ�����Ĵ������ͻ�ִ��)
		* �������������κι���֮��
			ONBUILD ADD . /app/src/
			ONBUILD RUN cd /app/src && make && make install
		* �������ᰴ�ո��������е�ָ��˳��ִ��,����ֻ�ܱ��̳�һ��(ֻ�����Ӿ���������,���������Ӿ���������)

		
		
					
		
		
			


	


	