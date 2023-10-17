model
  schema 1.1
type user
type system
  relations
    define systemAdmin: [user]
type group
  relations
    define adminSeller: [user] or systemAdmin from system
    define generalSeller: [user]
    define managerSeller: [user]
    define system: [system]
type product
  relations
    define can_delete: adminSeller from owner
    define can_read: can_update or generalSeller from owner
    define can_update: can_delete or managerSeller from owner
    define owner: [group]
