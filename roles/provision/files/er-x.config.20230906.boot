firewall {
    all-ping enable
    broadcast-ping disable
    group {
        network-group Internal-Networks {
            network 10.0.0.0/8
            network 192.168.0.0/16
            network 172.16.0.0/12
        }
    }
    ipv6-receive-redirects disable
    ipv6-src-route disable
    ip-src-route disable
    log-martians enable
    modify WAN_POLICY {
        rule 10 {
            action modify
            description "Skip LAN to LAN"
            destination {
                group {
                    network-group Internal-Networks
                }
            }
            modify {
                table main
            }
        }
        rule 70 {
            action modify
            modify {
                lb-group WAN-LB
            }
        }
    }
    name MGMT_PORT-IN {
        default-action drop
        enable-default-log
        rule 10 {
            action accept
            description "Allow SSH"
            destination {
                port 22
            }
            protocol tcp
        }
        rule 70 {
            action drop
            description "Drop icmp"
            icmp {
                type 8
            }
            log enable
            protocol icmp
            state {
                invalid enable
            }
        }
    }
    name WAN_INBOUND {
        default-action drop
        rule 10 {
            action accept
            description "Allow all established"
            protocol all
            state {
                established enable
                invalid disable
                new disable
                related enable
            }
        }
        rule 200 {
            action drop
            description "Drop the rest"
            protocol all
        }
    }
    receive-redirects disable
    send-redirects enable
    source-validation disable
    syn-cookies enable
}
interfaces {
    bridge br0 {
        address dhcp
        aging 300
        bridged-conntrack disable
        description ETH0.99+eth2.99_MGMT
        dhcp-options {
            client-option "option rfc3442-classless-static-routes code 121 = array of unsigned integer 8;"
            client-option "request rfc3442-classless-static-routes;"
            default-route update
            default-route-distance 210
            name-server no-update
        }
        hello-time 2
        max-age 20
        priority 32768
        promiscuous disable
        stp false
    }
    bridge br1 {
        aging 300
        bridged-conntrack disable
        description ETH0.39+eth2.39_MMNET_EXT
        hello-time 2
        max-age 20
        priority 32768
        promiscuous disable
        stp false
    }
    bridge br2 {
        address dhcp
        aging 300
        bridged-conntrack disable
        description eth1.99_MGMT
        dhcp-options {
            client-option "option rfc3442-classless-static-routes code 121 = array of unsigned integer 8;"
            client-option "request rfc3442-classless-static-routes;"
            default-route update
            default-route-distance 210
            name-server no-update
        }
        hello-time 2
        max-age 20
        priority 32768
        promiscuous disable
        stp false
    }
    bridge br3 {
        aging 300
        bridged-conntrack disable
        description eth1.39MMNET_EXT
        hello-time 2
        max-age 20
        priority 32768
        promiscuous disable
        stp false
    }
    bridge br4 {
        address 192.168.10.1/24
        aging 300
        bridged-conntrack disable
        description ETH2.29+ETH3_CLAN
        firewall {
            in {
                modify WAN_POLICY
            }
        }
        hello-time 2
        max-age 20
        priority 32768
        promiscuous disable
        stp false
    }
    ethernet eth0 {
        description PRIMARY_UPLINK_CO
        duplex auto
        poe {
            output 24v
        }
        speed auto
        vif 29 {
            address dhcp
            description MMNET
            dhcp-options {
                default-route update
                default-route-distance 210
                name-server no-update
            }
            firewall {
                in {
                    name WAN_INBOUND
                }
            }
        }
        vif 39 {
            bridge-group {
                bridge br1
            }
            description MMNET_EXT
        }
        vif 99 {
            bridge-group {
                bridge br0
            }
            description CPE_MGMT
        }
    }
    ethernet eth1 {
        description SECONDARY_UPLINK_CI
        duplex auto
        poe {
            output 24v
        }
        speed auto
        vif 29 {
            address dhcp
            description MMNET
            dhcp-options {
                default-route update
                default-route-distance 210
                name-server no-update
            }
            firewall {
                in {
                    name WAN_INBOUND
                }
            }
        }
        vif 39 {
            bridge-group {
                bridge br3
            }
            description MMNET_EXT
        }
        vif 99 {
            bridge-group {
                bridge br2
            }
            description CPE_MGMT
        }
    }
    ethernet eth2 {
        description FCPE
        duplex auto
        poe {
            output 24v
        }
        speed auto
        vif 29 {
            bridge-group {
                bridge br4
            }
            description CUSTOMER_LAN
            firewall {
                in {
                }
            }
        }
        vif 39 {
            bridge-group {
                bridge br1
            }
            description MMNET_EXT
        }
        vif 99 {
            bridge-group {
                bridge br0
            }
            description CPE_MGMT
        }
    }
    ethernet eth3 {
        bridge-group {
            bridge br4
        }
        description CUSTOMER_LAN
        duplex auto
        firewall {
            in {
            }
        }
        poe {
            output off
        }
        speed auto
    }
    ethernet eth4 {
        address 192.168.11.1/24
        description MGMT_PORT-IN
        duplex auto
        firewall {
            in {
                name MGMT_PORT-IN
            }
            local {
                name MGMT_PORT-IN
            }
        }
        poe {
            output off
        }
        speed auto
    }
    ethernet eth5 {
        disable
        duplex auto
        speed auto
    }
    loopback lo {
    }
    switch switch0 {
        mtu 1500
    }
}
load-balance {
    group WAN-LB {
        exclude-local-dns disable
        flush-on-active enable
        gateway-update-interval 20
        interface eth0.29 {
            route {
                default
            }
            route-test {
                count {
                    failure 3
                    success 4
                }
                initial-delay 60
                interval 5
                type {
                    ping {
                        target 8.8.8.8
                    }
                }
            }
        }
        interface eth1.29 {
            failover-only
            route {
                default
            }
            route-test {
                count {
                    failure 3
                    success 4
                }
                initial-delay 60
                interval 5
                type {
                    ping {
                        target 8.8.4.4
                    }
                }
            }
        }
        lb-local enable
        lb-local-metric-change enable
    }
}
protocols {
    static {
    }
}
service {
    dhcp-server {
        disabled false
        hostfile-update disable
        shared-network-name Customer_LAN {
            authoritative disable
            subnet 192.168.10.0/24 {
                default-router 192.168.10.1
                dns-server 192.168.10.1
                lease 86400
                start 192.168.10.100 {
                    stop 192.168.10.254
                }
            }
        }
        static-arp disable
        use-dnsmasq disable
    }
    dns {
        forwarding {
            cache-size 150
            dhcp eth0.29
            dhcp eth1.29
            listen-on eth2.29
            listen-on br4
            system
        }
    }
    gui {
        http-port 80
        https-port 443
        older-ciphers enable
    }
    nat {
        rule 5000 {
            description NAT_FOR_CO
            outbound-interface eth0.29
            protocol all
            source {
                address 192.168.10.0/24
            }
            type masquerade
        }
        rule 5001 {
            description NAT_FOR_CI
            outbound-interface eth1.29
            protocol all
            source {
                address 192.168.10.0/24
            }
            type masquerade
        }
    }
    snmp {
        community SBS-Infra {
            authorization ro
        }
        contact network-admin@frontiir.net
        listen-address 0.0.0.0 {
            port 16
        }
        location BI@Yangon
    }
    ssh {
        port 22
        protocol-version v2
    }
    ubnt-discover {
        disable
    }
    unms {
        connection wss://172.17.121.117:443+ZgNkKJxBnY6kf3RQUNBa6HpTLWpvmwVpSpFZuTk1BTlEtvAB+allowSelfSignedCertificate
    }
}
system {
    analytics-handler {
        send-analytics-report true
    }
    crash-handler {
        send-crash-report true
    }
    host-name CO000000-CI000000
    login {
        user operations {
            authentication {
                encrypted-password $5$dYGbiMHPZkSpQPsf$IdhxtFORWEM4h9vpLRn0vDO.9X7kC4tnvQWUiU9lW81
                plaintext-password ""
            }
            level admin
        }
    }
    name-server 59.153.88.210
    name-server 59.153.90.34
    ntp {
        server 172.17.121.22 {
        }
        server 172.17.121.23 {
        }
    }
    offload {
        hwnat enable
    }
    syslog {
        global {
            facility all {
                level notice
            }
            facility protocols {
                level debug
            }
        }
        host 172.17.121.159 {
            facility all {
                level info
            }
        }
    }
    time-zone Asia/Rangoon
}

/* Warning: Do not remove the following line. */
/* === vyatta-config-version: "config-management@1:conntrack@1:cron@1:dhcp-relay@1:dhcp-server@4:firewall@5:ipsec@5:nat@3:qos@1:quagga@2:suspend@1:system@5:ubnt-l2tp@1:ubnt-pptp@1:ubnt-udapi-server@1:ubnt-unms@2:ubnt-util@1:vrrp@1:vyatta-netflow@1:webgui@1:webproxy@1:zone-policy@1" === */
/* Release version: v2.0.9-hotfix.6.5574651.221230.1015 */
