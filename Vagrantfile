# -*- mode: ruby -*-
# vi: set ft=ruby :

# All Vagrant configuration is done below. The "2" in Vagrant.configure
# configures the configuration version (we support older styles for
# backwards compatibility). Please don't change it unless you know what
# you're doing.
Vagrant.configure("2") do |config|
  # The most common configuration options are documented and commented below.
  # For a complete reference, please see the online documentation at
  # https://docs.vagrantup.com.

  # CentOS boxes:
  # https://app.vagrantup.com/generic/boxes/rocky9
  config.vm.box = "generic/rocky9"

  # Create a forwarded port mapping which allows access to a specific port
  # within the machine from a port on the host machine. In the example below,
  # accessing "localhost:8080" will access port 80 on the guest machine.
  # NOTE: This will enable public access to the opened port
  # config.vm.network "forwarded_port", guest: 80, host: 8080
  config.vm.network "forwarded_port", guest: 80, host: 9080
  config.vm.network "forwarded_port", guest: 8082, host: 9082

  # Provider-specific configuration so you can fine-tune various
  # backing providers for Vagrant. These expose provider-specific options.
  config.vm.provider "virtualbox" do |vb|
    # Customize the amount of memory on the VM:
    vb.memory = "1024"
  end

  config.vm.provision "file", source: "scripts/visaoholandesa.service", destination: "/tmp/visaoholandesa/visaoholandesa.service"
  config.vm.provision "file", source: "scripts/visaoholandesa.conf", destination: "/tmp/visaoholandesa/visaoholandesa.conf"
  config.vm.provision "file", source: "build/libs/visaoholandesa-0.1-all.jar", destination: "/tmp/visaoholandesa/visaoholandesa-all.jar"

  # nginx installation reference:
  # https://www.digitalocean.com/community/tutorials/how-to-install-nginx-on-rocky-linux-9
  #
  # Java Rocky 9 packages:
  # https://docs.rockylinux.org/release_notes/9_0/#java-implementation
  config.vm.provision "shell", inline: <<-SHELL
    # Update installed packages
    dnf -y update

    # Install wget
    dnf update wget
    dnf install wget --assumeyes

    # Install vim
    dnf update vim
    dnf install vim --assumeyes

    # Install nginx
    dnf install nginx --assumeyes
    systemctl enable nginx
    systemctl start nginx

    # Adjust firewall for nginx
    firewall-cmd --permanent --add-service=http
    firewall-cmd --permanent --list-all
    firewall-cmd --reload

    # Checks nginx status
    systemctl status nginx

    # Install Java
    dnf install java-17-openjdk --assumeyes

    # Application SystemD Configuration
    groupadd -r appmgr
    useradd -r -s /bin/false -g appmgr jvmapps
    id jvmapps
    mkdir /opt/javaapps

    #
    # See config.vm.provision file copies way above this shell block
    #

    # Copies nginx.conf to have the same attributes for the app conf file.
    cp -v /etc/nginx/nginx.conf /etc/nginx/conf.d/visaoholandesa.conf
    cat /tmp/visaoholandesa/visaoholandesa.conf > /etc/nginx/conf.d/visaoholandesa.conf

    # Same as above. Make a copy to retain the expected file attributes.
    cp -v /usr/lib/systemd/system/nginx.service /opt/javaapps/visaoholandesa.service
    cat /tmp/visaoholandesa/visaoholandesa.service > /opt/javaapps/visaoholandesa.service
    pushd /opt/javaapps
      systemctl link ./visaoholandesa.service
    popd

    # Also copies nginx executable for the same reasons
    cp -v `which nginx` /opt/javaapps/visaoholandesa-all.jar
    cat /tmp/visaoholandesa/visaoholandesa-all.jar > /opt/javaapps/visaoholandesa-all.jar

    systemctl daemon-reload
    systemctl enable visaoholandesa.service
    systemctl start visaoholandesa.service
    systemctl status visaoholandesa.service

    # SELinux config
    setsebool -P httpd_can_network_connect 1

    # After adding the site-enabled configuration
    systemctl restart nginx
    systemctl status nginx
  SHELL
end

