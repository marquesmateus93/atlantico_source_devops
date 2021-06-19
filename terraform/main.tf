terraform {
  required_providers {
    google = {
      source = "hashicorp/google"
      version = "3.5.0"
    }
  }
}

provider "google" {
  credentials = file("sistema-marques-e87daa376312.json")

  project = "sistema-marques"
  region  = "us-central1"
  zone    = "us-central1-c"
}

resource "google_compute_instance" "vm_instance" {
  name         = "hello-world-machine"
  machine_type = "n1-standard-1"

  tags = ["http-server", "https-server", "docker"]

  boot_disk {
    initialize_params {
      image = "projects/sistema-marques/global/images/hello-world-image"
    }
  }

  #metadata_startup_script = "sudo yum install -y yum-utils; sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo; sudo yum install -y docker-ce docker-ce-cli containerd.io; sudo systemctl start docker"

  metadata = {
      ssh-keys = "jenkins:${file("~/Documentos/Atlântico/chaves/jenkins.pub")}"
  }

  network_interface {
    network = "default"
    access_config {
    }
  }
}