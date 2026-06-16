INSERT INTO proxy_node (
    name, 
    region, 
    vpn_type, 
    status, 
    leased_by_pod, 
    health_status, 
    fail_count,
    created_at, 
    updated_at
) VALUES (
    'laptop-ru-proxy', 
    'ru', 
    'OPENVPN', 
    'LEASED', 
    'laptop-proxy-pod', 
    'HEALTHY', 
    0,
    NOW(), 
    NOW()
) ON CONFLICT (name) DO UPDATE 
SET status = 'LEASED', leased_by_pod = 'laptop-proxy-pod', health_status = 'HEALTHY', fail_count = 0;
