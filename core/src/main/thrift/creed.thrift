include 'common.thrift'
include 'search.thrift'

namespace java com.goshoplane.creed.service
namespace js creed.service

service Creed {
  search.CatalogueSearchResults searchCatalogue(1: search.CatalogueSearchRequest searchRequest);
}