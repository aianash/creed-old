include 'common.thrift'

namespace java com.goshoplane.creed.search
namespace js neutrino.search

typedef string JSON

struct CreedScore {
  1: double value;
}

struct CatalogueResultEntry {
  1: common.CatalogueItemId itemId;
  2: CreedScore score;
}

struct CatalogueSearchResults {
  1: common.UserId userId;
  2: list<CatalogueResultEntry> results;
}

struct QueryParamWeight {
  1: double value;
}

struct QueryParam {
  1: optional JSON json;
  2: optional binary stream;
  3: optional string value;
  4: optional QueryParamWeight weight = {"value" : 1.0};
}

struct CatalogueSearchQuery {
  1: map<string, QueryParam> params;
  2: string queryText;
}

struct CatalogueSearchRequest {
  1: common.UserId userId;
  2: CatalogueSearchQuery query;
}