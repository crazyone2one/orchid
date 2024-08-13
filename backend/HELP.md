# 方法使用注解

- 单个权限

  ~~~
  @PreAuthorize("@auth.hasAuthority('ORGANIZATION_MEMBER:READ,ORGANIZATION_MEMBER:READ')")
  ~~~

- 单个权限

  ~~~
  @HasAuthorize(value = PermissionConstants.SYSTEM_USER_ROLE_READ)
  ~~~

- 多个权限--and

  ~~~
  @PreAuthorize("@auth.hasAuthority('ORGANIZATION_MEMBER:READ,ORGANIZATION_MEMBER:READ') and @auth.hasAuthority('x')")
  ~~~

  

- 多个权限--or

  ~~~
  @PreAuthorize("@auth.hasAuthority('ORGANIZATION_MEMBER:READ,ORGANIZATION_MEMBER:READ') or @auth.hasAuthority('x')")
  ~~~

  