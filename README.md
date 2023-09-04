## Prerequisites

1. Make sure you update [inventory](./inventory) file before running the playbook.

   For example, update node name and SSH host IP address.

   ```
   [web]
   ubuntu 
   ```
## Peplace Real Provision file

1. Make sure you update [roles/provision/files/provision-for-fcpe.tar](.realfile) file before running the playbook

   ```
   rm -rf provision-for-fcpe.tar
   replace ( real file )
   ``` 

## Useful commands

**check connection
```bash
ansible -m ping all
```

**Playbook syntax check**
```bash
ansible-playbook -v --syntax-check main.yaml
```

**Run playbook**

```bash
ansible-playbook -v main.yaml
```

# Ansible_with_pip
# Ansible_with_pip
