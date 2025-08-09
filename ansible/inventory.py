#!/usr/bin/env python3
import os
import json

def get_inventory():
    inventory = {
        'medimind_servers': {
            'hosts': ['medimind-app'],
            'vars': {
                'ansible_host': os.environ.get('EC2APP_IP', 'localhost'),
                'ansible_user': os.environ.get('EC2APP_USER', 'ubuntu'),
                'ansible_ssh_private_key_file': '~/.ssh/ec2app-key.pem',
                'ansible_python_interpreter': '/usr/bin/python3'
            }
        },
        '_meta': {
            'hostvars': {
                'medimind-app': {
                    'ansible_host': os.environ.get('EC2APP_IP', 'localhost'),
                    'ansible_user': os.environ.get('EC2APP_USER', 'ubuntu'),
                    'ansible_ssh_private_key_file': '~/.ssh/ec2app-key.pem',
                    'ansible_python_interpreter': '/usr/bin/python3'
                }
            }
        }
    }
    return inventory

if __name__ == '__main__':
    print(json.dumps(get_inventory(), indent=2))
